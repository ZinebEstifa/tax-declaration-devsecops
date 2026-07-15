package com.example.Declarations.fiscaux.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_fiscal", nullable = false, unique = true, length = 50)
    private String numeroFiscal;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash; // hash Argon2id complet (contient deja le salt)

    @Column(name = "raison_sociale")
    private String raisonSociale;

    @Builder.Default
    @Column(name = "tentatives_echouees", nullable = false)
    private int tentativesEchouees = 0;

    @Builder.Default
    @Column(name = "compte_bloque", nullable = false)
    private boolean compteBloque = false;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
    }
}