package com.bibliotheque.service;

import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.UtilisateurRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public Utilisateur login(String email, String rawPassword) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email.trim());
        if (userOpt.isPresent()) {
            Utilisateur u = userOpt.get();
            if (u.getActif()) {
                try {
                    if (BCrypt.checkpw(rawPassword, u.getMotDePasse())) {
                        return u;
                    }
                } catch (IllegalArgumentException e) {
                    // If the password in DB is plain text, BCrypt will throw IllegalArgumentException.
                    // We can check if it matches plain text for migration purposes, or just fail.
                    if (rawPassword.equals(u.getMotDePasse())) {
                        return u;
                    }
                }
            }
        }
        return null;
    }

    public Utilisateur register(Utilisateur user, String rawPassword) {
        if (utilisateurRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé.");
        }
        
        user.setMotDePasse(BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
        user.setRole("membre");
        
        // Generate member number M-YYYY-XXXX
        String year = String.valueOf(Year.now().getValue());
        String randomHex = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        user.setNumeroMembre("M-" + year + "-" + randomHex);
        
        return utilisateurRepository.save(user);
    }
}
