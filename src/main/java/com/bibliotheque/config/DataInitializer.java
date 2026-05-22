package com.bibliotheque.config;

import com.bibliotheque.model.Parametre;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.repository.ParametreRepository;
import com.bibliotheque.repository.UtilisateurRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UtilisateurRepository utilisateurRepository, ParametreRepository parametreRepository) {
        return args -> {
            // Seed or Update Admin User
            Optional<Utilisateur> adminOpt = utilisateurRepository.findByEmail("admin@bibliotheque.com");
            Utilisateur admin = adminOpt.orElse(new Utilisateur());
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setEmail("admin@bibliotheque.com");
            // Force reset the password to BCrypt hash of "admin123" so it always works
            admin.setMotDePasse(BCrypt.hashpw("admin123", BCrypt.gensalt(10)));
            admin.setRole("admin");
            admin.setActif(true);
            utilisateurRepository.save(admin);

            // Seed Parametres
            if (parametreRepository.findById("amende_journaliere").isEmpty()) {
                Parametre p = new Parametre("amende_journaliere", "0.50");
                parametreRepository.save(p);
            }
        };
    }
}
