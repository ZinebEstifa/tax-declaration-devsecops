package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.dto.ChargeRequest;
import com.example.Declarations.fiscaux.dto.ChargeResponse;
import com.example.Declarations.fiscaux.entity.Charge;
import com.example.Declarations.fiscaux.entity.Declaration;
import com.example.Declarations.fiscaux.entity.StatutDeclaration;
import com.example.Declarations.fiscaux.entity.Utilisateur;
import com.example.Declarations.fiscaux.repository.ChargeRepository;
import com.example.Declarations.fiscaux.repository.DeclarationRepository;
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
class ChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private DeclarationRepository declarationRepository;

    @Mock
    private DeclarationService declarationService;

    @InjectMocks
    private ChargeService chargeService;

    private Declaration mockDeclaration;

    @BeforeEach
    void setUp() {
        Utilisateur mockUser = Utilisateur.builder().id(1L).numeroFiscal("12345678").build();
        mockDeclaration = Declaration.builder()
                .id(200L)
                .utilisateur(mockUser)
                .statut(StatutDeclaration.BROUILLON)
                .dateDebutExercice(LocalDate.of(2025, 1, 1))
                .dateFinExercice(LocalDate.of(2025, 12, 31))
                .produits(new ArrayList<>())
                .charges(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Ajout d'une charge avec recalcul des totaux")
    void testAddCharge_Success() {
        when(declarationService.getDeclarationEntity(200L, "12345678")).thenReturn(mockDeclaration);
        when(chargeRepository.save(any(Charge.class))).thenAnswer(inv -> {
            Charge c = inv.getArgument(0);
            c.setId(12L);
            return c;
        });

        ChargeRequest req = new ChargeRequest("Loyer bureau", new BigDecimal("8000.00"));
        ChargeResponse res = chargeService.addCharge(200L, req, "12345678");

        assertNotNull(res);
        assertEquals(12L, res.getId());
        assertEquals("Loyer bureau", res.getLibelle());
        assertEquals(new BigDecimal("8000.00"), res.getMontant());

        verify(declarationService, times(1)).recalculateTotals(mockDeclaration);
        verify(declarationRepository, times(1)).save(mockDeclaration);
    }

    @Test
    @DisplayName("Ajout de charge sur une déclaration déposée (Rectification)")
    void testAddCharge_OnDepositedDeclaration() {
        mockDeclaration.setStatut(StatutDeclaration.DEPOSEE);
        when(declarationService.getDeclarationEntity(200L, "12345678")).thenReturn(mockDeclaration);
        when(chargeRepository.save(any(Charge.class))).thenAnswer(inv -> {
            Charge c = inv.getArgument(0);
            c.setId(15L);
            return c;
        });

        ChargeRequest req = new ChargeRequest("Fournitures complémentaires", new BigDecimal("1200"));
        ChargeResponse res = chargeService.addCharge(200L, req, "12345678");

        assertNotNull(res);
        assertEquals(15L, res.getId());
        assertEquals("Fournitures complémentaires", res.getLibelle());
        verify(declarationService, times(1)).recalculateTotals(mockDeclaration);
    }

    @Test
    @DisplayName("Suppression d'une charge")
    void testDeleteCharge_Success() {
        Charge charge = Charge.builder().id(9L).declaration(mockDeclaration).libelle("Électricité").montant(new BigDecimal("500")).build();
        mockDeclaration.getCharges().add(charge);

        when(chargeRepository.findById(9L)).thenReturn(Optional.of(charge));
        when(declarationService.getDeclarationEntity(200L, "12345678")).thenReturn(mockDeclaration);

        chargeService.deleteCharge(9L, "12345678");

        assertFalse(mockDeclaration.getCharges().contains(charge));
        verify(chargeRepository, times(1)).delete(charge);
        verify(declarationService, times(1)).recalculateTotals(mockDeclaration);
    }
}
