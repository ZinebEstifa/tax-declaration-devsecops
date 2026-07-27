package com.example.Declarations.fiscaux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProduitResponse {
    private Long id;
    private String libelle;
    private BigDecimal montant;
}
