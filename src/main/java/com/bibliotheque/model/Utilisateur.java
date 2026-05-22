package com.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(length = 20)
    private String telephone;

    @Column(nullable = false, length = 20)
    private String role; // 'admin' or 'membre'

    @Column(name = "numero_membre", unique = true, length = 50)
    private String numeroMembre; // M-YYYY-XXXX (only for member role)

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(name = "date_inscription", nullable = false, updatable = false)
    private LocalDateTime dateInscription;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @PrePersist
    protected void onCreate() {
        dateInscription = LocalDateTime.now();
    }
}
