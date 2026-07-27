package com.example.Declarations.fiscaux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationResponse {
    private Long id;
    private LocalDate dateDebutExercice;
    private LocalDate dateFinExercice;
    private LocalDate dateDepot;
    private BigDecimal totalProduits;
    private BigDecimal totalCharges;
    private BigDecimal resultatNet;
    private BigDecimal montantImpot;
    private BigDecimal penaliteRetard;
    private String statut;
    private List<ProduitResponse> produits;
    private List<ChargeResponse> charges;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
