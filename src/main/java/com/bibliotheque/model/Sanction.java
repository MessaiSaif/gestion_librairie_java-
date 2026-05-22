package com.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "membre_id", nullable = false)
    private Utilisateur membre;

    @ManyToOne
    @JoinColumn(name = "emprunt_id", nullable = false)
    private Emprunt emprunt;

    @Column(nullable = false)
    private Double montant;

    @Column(name = "date_sanction", nullable = false)
    private LocalDateTime dateSanction;

    @Column(nullable = false)
    private Boolean payee = false;

    @PrePersist
    protected void onCreate() {
        if (dateSanction == null) {
            dateSanction = LocalDateTime.now();
        }
    }
}
