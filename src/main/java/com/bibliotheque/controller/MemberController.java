package com.bibliotheque.controller;

import com.bibliotheque.model.Categorie;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.Sanction;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.repository.CategorieRepository;
import com.bibliotheque.repository.LivreRepository;
import com.bibliotheque.repository.ReservationRepository;
import com.bibliotheque.repository.SanctionRepository;
import com.bibliotheque.repository.EmpruntRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import com.bibliotheque.model.Utilisateur;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.time.LocalDate;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SanctionRepository sanctionRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private boolean checkMemberAuth(HttpSession session) {
        return session.getAttribute("user_id") != null && "membre".equals(session.getAttribute("user_role"));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkMemberAuth(session)) return "redirect:/auth/login";
        
        Integer userId = (Integer) session.getAttribute("user_id");
        List<Reservation> notifications = reservationRepository.findByMembreIdAndStatut(userId, "notifie");
        List<Sanction> sanctions = sanctionRepository.findByMembreIdAndPayee(userId, false);
        
        initPanier(session);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("sanctions", sanctions);
        return "dashboard-membre";
    }

    @GetMapping("/catalogue")
    public String catalogue(@RequestParam(required = false) Integer categorie_id, HttpSession session, Model model) {
        if (!checkMemberAuth(session)) return "redirect:/auth/login";
        initPanier(session);

        List<Categorie> categories = categorieRepository.findAllByOrderByNomAsc();
        List<Livre> livres = (categorie_id != null) 
                ? livreRepository.findByCategorieId(categorie_id) 
                : livreRepository.findAllByOrderByTitreAsc();

        List<Integer> panier = (List<Integer>) session.getAttribute("panier");

        // Pre-compute emprunt counts for each book
        java.util.Map<Integer, Long> empruntCounts = new java.util.HashMap<>();
        for (Livre livre : livres) {
            long count = empruntRepository.countByLivreIdAndStatutIn(livre.getId(), Arrays.asList("en_cours", "en_retard"));
            empruntCounts.put(livre.getId(), count);
        }

        model.addAttribute("categories", categories);
        model.addAttribute("livres", livres);
        model.addAttribute("filtre_categorie", categorie_id);
        model.addAttribute("panier_ids", panier);
        model.addAttribute("empruntCounts", empruntCounts);
        return "catalogue";
    }

    @PostMapping("/catalogue")
    public String reserverLivre(@RequestParam Integer livre_id, @RequestParam String action,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkMemberAuth(session)) return "redirect:/auth/login";
        Integer userId = (Integer) session.getAttribute("user_id");
        if ("reserver".equals(action)) {
            Utilisateur membre = utilisateurRepository.findById(userId).orElse(null);
            Livre livre = livreRepository.findById(livre_id).orElse(null);
            if (membre != null && livre != null) {
                boolean alreadyReserved = reservationRepository.findByMembreIdAndLivreIdAndStatutIn(
                        userId, livre_id, Arrays.asList("en_attente", "notifie")).size() > 0;
                if (!alreadyReserved) {
                    Reservation r = new Reservation();
                    r.setMembre(membre);
                    r.setLivre(livre);
                    r.setStatut("en_attente");
                    reservationRepository.save(r);
                    redirectAttributes.addFlashAttribute("success_msg", "Réservation confirmée !");
                } else {
                    redirectAttributes.addFlashAttribute("error_msg", "Vous avez déjà réservé ce livre.");
                }
            }
        }
        return "redirect:/member/catalogue";
    }

    @PostMapping("/panier")
    public String gererPanier(@RequestParam String action, @RequestParam(required = false) Integer livre_id,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkMemberAuth(session)) return "redirect:/auth/login";
        initPanier(session);
        Integer userId = (Integer) session.getAttribute("user_id");
        List<Integer> panier = (List<Integer>) session.getAttribute("panier");

        if ("ajouter".equals(action) && livre_id != null) {
            if (!panier.contains(livre_id)) {
                panier.add(livre_id);
                redirectAttributes.addFlashAttribute("success_msg", "Livre ajouté au panier.");
            }
            return "redirect:/member/catalogue";
        } else if ("retirer".equals(action) && livre_id != null) {
            panier.remove(livre_id);
            redirectAttributes.addFlashAttribute("success_msg", "Livre retiré du panier.");
        } else if ("confirmer".equals(action)) {
            long unpaidFines = sanctionRepository.findByMembreIdAndPayee(userId, false).size();
            if (unpaidFines > 0) {
                redirectAttributes.addFlashAttribute("error_msg", "Vous ne pouvez pas emprunter car vous avez des sanctions non réglées.");
                return "redirect:/member/dashboard";
            }

            if (panier.isEmpty()) {
                redirectAttributes.addFlashAttribute("error_msg", "Votre panier est vide.");
            } else {
                int empruntsReussis = 0;
                List<String> erreurs = new ArrayList<>();
                Utilisateur membre = utilisateurRepository.findById(userId).orElse(null);

                List<Integer> toRemove = new ArrayList<>();
                for (Integer id : panier) {
                    Livre livre = livreRepository.findById(id).orElse(null);
                    if (livre != null) {
                        long currentBorrowed = empruntRepository.countByLivreIdAndStatutIn(id, Arrays.asList("en_cours", "en_retard"));
                        if (livre.getNombreExemplaires() - currentBorrowed > 0) {
                            Emprunt e = new Emprunt();
                            e.setMembre(membre);
                            e.setLivre(livre);
                            e.setDateRetourPrevue(LocalDate.now().plusDays(14));
                            e.setStatut("en_cours");
                            empruntRepository.save(e);
                            empruntsReussis++;
                            toRemove.add(id);
                        } else {
                            erreurs.add("Le livre '" + livre.getTitre() + "' n'est plus disponible.");
                        }
                    }
                }
                panier.removeAll(toRemove);
                if (erreurs.isEmpty() && empruntsReussis > 0) {
                    redirectAttributes.addFlashAttribute("success_msg", "Tous vos emprunts ont été confirmés avec succès !");
                    return "redirect:/member/emprunts";
                } else {
                    redirectAttributes.addFlashAttribute("error_msg", "Certains emprunts ont échoué : " + String.join(" ", erreurs));
                }
            }
        }
        return "redirect:/member/panier";
    }

    @GetMapping("/panier")
    public String panier(HttpSession session, Model model) {
        if (!checkMemberAuth(session)) return "redirect:/auth/login";
        initPanier(session);
        
        List<Integer> panierIds = (List<Integer>) session.getAttribute("panier");
        List<Livre> livresPanier = new ArrayList<>();
        if (!panierIds.isEmpty()) {
            livresPanier = livreRepository.findAllById(panierIds);
        }
        
        model.addAttribute("livres_panier", livresPanier);
        return "panier";
    }

    @GetMapping("/emprunts")
    public String mesEmprunts(HttpSession session, Model model) {
        if (!checkMemberAuth(session)) return "redirect:/auth/login";
        Integer userId = (Integer) session.getAttribute("user_id");
        List<Emprunt> emprunts = empruntRepository.findByMembreId(userId);
        model.addAttribute("emprunts", emprunts);
        return "mes-emprunts";
    }

    private void initPanier(HttpSession session) {
        if (session.getAttribute("panier") == null) {
            session.setAttribute("panier", new ArrayList<Integer>());
        }
    }
}
