package com.example.Declarations.fiscaux.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitRequest {

    @NotBlank(message = "Le libellé du produit est obligatoire")
    private String libelle;

    @NotNull(message = "Le montant du produit est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le montant doit être positif")
    private BigDecimal montant;
}
