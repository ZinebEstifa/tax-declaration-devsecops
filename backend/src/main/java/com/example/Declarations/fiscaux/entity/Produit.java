package com.example.Declarations.fiscaux.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id", nullable = false)
    @JsonIgnore
    private Declaration declaration;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;
}
