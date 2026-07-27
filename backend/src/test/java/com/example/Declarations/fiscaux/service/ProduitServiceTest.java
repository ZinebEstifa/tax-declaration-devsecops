package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.dto.ProduitRequest;
import com.example.Declarations.fiscaux.dto.ProduitResponse;
import com.example.Declarations.fiscaux.entity.Declaration;
import com.example.Declarations.fiscaux.entity.Produit;
import com.example.Declarations.fiscaux.entity.StatutDeclaration;
import com.example.Declarations.fiscaux.entity.Utilisateur;
import com.example.Declarations.fiscaux.repository.DeclarationRepository;
import com.example.Declarations.fiscaux.repository.ProduitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private DeclarationRepository declarationRepository;

    @Mock
    private DeclarationService declarationService;

    @InjectMocks
    private ProduitService produitService;

    private Declaration mockDeclaration;

    @BeforeEach
    void setUp() {
        Utilisateur mockUser = Utilisateur.builder().id(1L).numeroFiscal("12345678").build();
        mockDeclaration = Declaration.builder()
                .id(100L)
                .utilisateur(mockUser)
                .statut(StatutDeclaration.BROUILLON)
                .dateDebutExercice(LocalDate.of(2025, 1, 1))
                .dateFinExercice(LocalDate.of(2025, 12, 31))
                .produits(new ArrayList<>())
                .charges(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Ajout d'un produit avec recalcul des totaux")
    void testAddProduit_Success() {
        when(declarationService.getDeclarationEntity(100L, "12345678")).thenReturn(mockDeclaration);
        when(produitRepository.save(any(Produit.class))).thenAnswer(inv -> {
            Produit p = inv.getArgument(0);
            p.setId(5L);
            return p;
        });

        ProduitRequest req = new ProduitRequest("Service informatique", new BigDecimal("15000.00"));
        ProduitResponse res = produitService.addProduit(100L, req, "12345678");

        assertNotNull(res);
        assertEquals(5L, res.getId());
        assertEquals("Service informatique", res.getLibelle());
        assertEquals(new BigDecimal("15000.00"), res.getMontant());

        verify(declarationService, times(1)).recalculateTotals(mockDeclaration);
        verify(declarationRepository, times(1)).save(mockDeclaration);
    }

    @Test
    @DisplayName("Ajout d'un produit sur une déclaration déposée (Rectification)")
    void testAddProduit_OnDepositedDeclaration() {
        mockDeclaration.setStatut(StatutDeclaration.DEPOSEE);
        when(declarationService.getDeclarationEntity(100L, "12345678")).thenReturn(mockDeclaration);
        when(produitRepository.save(any(Produit.class))).thenAnswer(inv -> {
            Produit p = inv.getArgument(0);
            p.setId(8L);
            return p;
        });

        ProduitRequest req = new ProduitRequest("Ventes complémentaires", new BigDecimal("5000"));
        ProduitResponse res = produitService.addProduit(100L, req, "12345678");

        assertNotNull(res);
        assertEquals(8L, res.getId());
        assertEquals("Ventes complémentaires", res.getLibelle());
        verify(declarationService, times(1)).recalculateTotals(mockDeclaration);
    }

    @Test
    @DisplayName("Suppression d'un produit")
    void testDeleteProduit_Success() {
        Produit produit = Produit.builder().id(7L).declaration(mockDeclaration).libelle("Prestation").montant(new BigDecimal("1000")).build();
        mockDeclaration.getProduits().add(produit);

        when(produitRepository.findById(7L)).thenReturn(Optional.of(produit));
        when(declarationService.getDeclarationEntity(100L, "12345678")).thenReturn(mockDeclaration);

        produitService.deleteProduit(7L, "12345678");

        assertFalse(mockDeclaration.getProduits().contains(produit));
        verify(produitRepository, times(1)).delete(produit);
        verify(declarationService, times(1)).recalculateTotals(mockDeclaration);
    }
}
