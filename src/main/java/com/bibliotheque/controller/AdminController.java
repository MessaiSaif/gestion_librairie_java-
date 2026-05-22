package com.bibliotheque.controller;

import com.bibliotheque.model.Categorie;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Livre;
import com.bibliotheque.model.Reservation;
import com.bibliotheque.model.Sanction;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LivreRepository livreRepository;
    
    @Autowired
    private CategorieRepository categorieRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private EmpruntRepository empruntRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private SanctionRepository sanctionRepository;

    private boolean checkAdminAuth(HttpSession session) {
        return session.getAttribute("user_id") != null && "admin".equals(session.getAttribute("user_role"));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        return "dashboard-admin";
    }

    @GetMapping("/livres")
    public String adminLivres(@RequestParam(required = false) String action, 
                              @RequestParam(required = false) Integer edit, 
                              HttpSession session, Model model) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        
        List<Livre> livres = livreRepository.findAllByOrderByTitreAsc();
        List<Categorie> categories = categorieRepository.findAllByOrderByNomAsc();
        
        model.addAttribute("livres", livres);
        model.addAttribute("categories", categories);
        
        if ("add".equals(action)) {
            model.addAttribute("edit_livre", new Livre());
            model.addAttribute("isEdit", false);
        } else if (edit != null) {
            livreRepository.findById(edit).ifPresent(l -> {
                model.addAttribute("edit_livre", l);
                model.addAttribute("isEdit", true);
            });
        }
        return "admin-livres";
    }

    @PostMapping("/livres")
    public String saveLivre(@ModelAttribute Livre livre, @RequestParam Integer categorie_id, 
                            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            Categorie cat = categorieRepository.findById(categorie_id).orElse(null);
            livre.setCategorie(cat);
            if (livre.getId() == null) {
                livre.setDateAjout(LocalDateTime.now());
            }
            livreRepository.save(livre);
            redirectAttributes.addFlashAttribute("success_msg", "Livre enregistré avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Erreur lors de l'enregistrement.");
        }
        return "redirect:/admin/livres";
    }

    @GetMapping(value = "/livres", params = "delete")
    public String deleteLivre(@RequestParam Integer delete, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            livreRepository.deleteById(delete);
            redirectAttributes.addFlashAttribute("success_msg", "Livre supprimé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Impossible de supprimer ce livre (liens existants).");
        }
        return "redirect:/admin/livres";
    }

    @GetMapping("/emprunts")
    public String adminEmprunts(HttpSession session, Model model) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        List<Emprunt> emprunts = empruntRepository.findByStatutIn(Arrays.asList("en_cours", "en_retard"));
        model.addAttribute("emprunts", emprunts);
        return "admin-emprunts";
    }

    @PostMapping(value = "/emprunts", params = "action=valider_retour")
    public String validerRetour(@RequestParam Integer emprunt_id, @RequestParam Integer livre_id, 
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            Emprunt emprunt = empruntRepository.findById(emprunt_id).orElseThrow();
            emprunt.setStatut("retourne");
            emprunt.setDateRetourEffective(LocalDate.now());
            empruntRepository.save(emprunt);

            Optional<Sanction> sanc = sanctionRepository.findByEmpruntIdAndPayee(emprunt_id, false);
            sanc.ifPresent(s -> {
                s.setPayee(true);
                sanctionRepository.save(s);
            });

            List<Reservation> file = reservationRepository.findByLivreIdAndStatutOrderByDateReservationAsc(livre_id, "en_attente");
            if (!file.isEmpty()) {
                Reservation next = file.get(0);
                next.setStatut("notifie");
                next.setDateNotification(LocalDateTime.now());
                reservationRepository.save(next);
                redirectAttributes.addFlashAttribute("success_msg", "Retour validé. Le membre en attente a été notifié.");
            } else {
                redirectAttributes.addFlashAttribute("success_msg", "Retour validé avec succès !");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Erreur lors de la validation du retour.");
        }
        return "redirect:/admin/emprunts";
    }

    @GetMapping(value = "/emprunts", params = "action=valider_retour")
    public String validerRetourGet(@RequestParam Integer emprunt_id, @RequestParam Integer livre_id, 
                                HttpSession session, RedirectAttributes redirectAttributes) {
        return validerRetour(emprunt_id, livre_id, session, redirectAttributes);
    }

    @GetMapping("/categories")
    public String adminCategories(@RequestParam(required = false) Integer edit, HttpSession session, Model model) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        model.addAttribute("categories", categorieRepository.findAllByOrderByNomAsc());
        if (edit != null) {
            model.addAttribute("edit_cat", categorieRepository.findById(edit).orElse(null));
        }
        return "admin-categories";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute Categorie categorie, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            categorieRepository.save(categorie);
            redirectAttributes.addFlashAttribute("success_msg", "Catégorie enregistrée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Erreur d'enregistrement.");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping(value = "/categories", params = "delete")
    public String deleteCategory(@RequestParam Integer delete, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            categorieRepository.deleteById(delete);
            redirectAttributes.addFlashAttribute("success_msg", "Catégorie supprimée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Impossible de supprimer (liée à des livres).");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/membres")
    public String adminMembres(@RequestParam(required = false) String action, 
                               @RequestParam(required = false) Integer edit, 
                               HttpSession session, Model model) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        model.addAttribute("membres", utilisateurRepository.findByRole("membre"));
        
        if ("add".equals(action)) {
            model.addAttribute("edit_membre", new Utilisateur());
            model.addAttribute("isEdit", false);
        } else if (edit != null) {
            utilisateurRepository.findById(edit).ifPresent(m -> {
                model.addAttribute("edit_membre", m);
                model.addAttribute("isEdit", true);
            });
        }
        return "admin-membres";
    }

    @PostMapping("/membres")
    public String saveMembre(@ModelAttribute Utilisateur membre, @RequestParam(required = false) String password, 
                             HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            if (membre.getId() == null) {
                // New member
                if (utilisateurRepository.findByEmail(membre.getEmail()).isPresent()) {
                    redirectAttributes.addFlashAttribute("error_msg", "Email déjà utilisé.");
                    return "redirect:/admin/membres?action=add";
                }
                membre.setRole("membre");
                String year = String.valueOf(java.time.Year.now().getValue());
                String randomHex = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                membre.setNumeroMembre("M-" + year + "-" + randomHex);
                if (password != null && !password.isEmpty()) {
                    membre.setMotDePasse(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(10)));
                } else {
                    membre.setMotDePasse(org.mindrot.jbcrypt.BCrypt.hashpw("default123", org.mindrot.jbcrypt.BCrypt.gensalt(10)));
                }
            } else {
                // Update member
                Utilisateur existing = utilisateurRepository.findById(membre.getId()).orElseThrow();
                existing.setNom(membre.getNom());
                existing.setPrenom(membre.getPrenom());
                existing.setEmail(membre.getEmail());
                existing.setTelephone(membre.getTelephone());
                existing.setActif(membre.getActif());
                if (password != null && !password.isEmpty()) {
                    existing.setMotDePasse(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(10)));
                }
                membre = existing;
            }
            utilisateurRepository.save(membre);
            redirectAttributes.addFlashAttribute("success_msg", "Membre enregistré avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
        return "redirect:/admin/membres";
    }

    @GetMapping(value = "/membres", params = "delete")
    public String deleteMembre(@RequestParam Integer delete, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        try {
            utilisateurRepository.deleteById(delete);
            redirectAttributes.addFlashAttribute("success_msg", "Membre supprimé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error_msg", "Impossible de supprimer ce membre (liens avec emprunts ou réservations existants).");
        }
        return "redirect:/admin/membres";
    }

    @GetMapping("/historique")
    public String adminHistorique(HttpSession session, Model model) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        model.addAttribute("emprunts", empruntRepository.findAll());
        return "admin-historique";
    }

    @GetMapping("/sanctions")
    public String adminSanctions(HttpSession session, Model model) {
        if (!checkAdminAuth(session)) return "redirect:/auth/login";
        model.addAttribute("sanctions", sanctionRepository.findAll());
        return "admin-sanctions";
    }
}
