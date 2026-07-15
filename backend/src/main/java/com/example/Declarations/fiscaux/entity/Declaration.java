package com.example.Declarations.fiscaux.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "declarations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @JsonIgnore
    private Utilisateur utilisateur;

    // --- Exercice fiscal (Page 2) ---
    @Column(name = "date_debut_exercice", nullable = false)
    private LocalDate dateDebutExercice;

    @Column(name = "date_fin_exercice", nullable = false)
    private LocalDate dateFinExercice;

    @Column(name = "date_depot")
    private LocalDate dateDepot;

    // --- Resultats calcules (Page 3) ---
    @Column(name = "total_produits", precision = 15, scale = 2)
    private BigDecimal totalProduits;

    @Column(name = "total_charges", precision = 15, scale = 2)
    private BigDecimal totalCharges;

    @Column(name = "resultat_net", precision = 15, scale = 2)
    private BigDecimal resultatNet;

    @Column(name = "montant_impot", precision = 15, scale = 2)
    private BigDecimal montantImpot;

    @Column(name = "penalite_retard", precision = 15, scale = 2)
    private BigDecimal penaliteRetard;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutDeclaration statut = StatutDeclaration.BROUILLON;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Produit> produits = new ArrayList<>();

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Charge> charges = new ArrayList<>();

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}