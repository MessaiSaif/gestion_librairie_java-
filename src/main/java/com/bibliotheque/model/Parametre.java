package com.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "parametres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parametre {

    @Id
    @Column(length = 50)
    private String cle;

    @Column(nullable = false, length = 255)
    private String valeur;
}
