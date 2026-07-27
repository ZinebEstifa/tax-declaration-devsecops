package com.example.Declarations.fiscaux.entity;

public enum StatutDeclaration {
    BROUILLON,   // en cours de saisie, modifiable
    VALIDEE,     // contrôlée et validée par le contribuable
    DEPOSEE,     // déposée officiellement
    RECTIFIEE    // rectifiée suite à modification post-dépôt
}
