package com.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "livres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "annee_publication")
    private Integer anneePublication;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "nombre_exemplaires", nullable = false)
    private Integer nombreExemplaires = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_ajout", nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    @PrePersist
    protected void onCreate() {
        dateAjout = LocalDateTime.now();
    }
}
