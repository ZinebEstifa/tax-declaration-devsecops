package com.example.Declarations.fiscaux.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationRequest {

    @NotNull(message = "La date de début d'exercice est obligatoire")
    private LocalDate dateDebutExercice;

    @NotNull(message = "La date de fin d'exercice est obligatoire")
    private LocalDate dateFinExercice;

    @Valid
    private List<ProduitRequest> produits;

    @Valid
    private List<ChargeRequest> charges;
}
