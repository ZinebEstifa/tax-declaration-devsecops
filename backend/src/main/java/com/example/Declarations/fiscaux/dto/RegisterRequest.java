package com.example.Declarations.fiscaux.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Le numéro fiscal est obligatoire.")
    private String numFiscal;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    private String password;

    private String raisonSociale;
}
