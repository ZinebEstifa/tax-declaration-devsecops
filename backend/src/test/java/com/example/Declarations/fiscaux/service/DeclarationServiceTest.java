package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.dto.ChargeRequest;
import com.example.Declarations.fiscaux.dto.DeclarationRequest;
import com.example.Declarations.fiscaux.dto.DeclarationResponse;
import com.example.Declarations.fiscaux.dto.ProduitRequest;
import com.example.Declarations.fiscaux.entity.Declaration;
import com.example.Declarations.fiscaux.entity.StatutDeclaration;
import com.example.Declarations.fiscaux.entity.Utilisateur;
import com.example.Declarations.fiscaux.exception.BadRequestException;
import com.example.Declarations.fiscaux.repository.DeclarationRepository;
import com.example.Declarations.fiscaux.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeclarationServiceTest {

    @Mock
    private DeclarationRepository declarationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeclarationService declarationService;

    private Utilisateur mockUser;

    @BeforeEach
    void setUp() {
        mockUser = Utilisateur.builder()
                .id(1L)
                .numeroFiscal("12345678")
                .raisonSociale("Test Enterprise")
                .build();
    }

    @Test
    @DisplayName("Calcul de l'impôt progressif - Tranche 1 (<= 100 000 DH)")
    void testTaxCalculation_Tranche1() {
        BigDecimal resultatNet = new BigDecimal("80000.00");
        BigDecimal tax = declarationService.calculateProgressiveTax(resultatNet);
        assertEquals(new BigDecimal("8000.00"), tax);
    }

    @Test
    @DisplayName("Calcul de l'impôt progressif - Tranche 2 (100 001 à 1 000 000 DH)")
    void testTaxCalculation_Tranche2() {
        BigDecimal resultatNet = new BigDecimal("500000.00");
        BigDecimal tax = declarationService.calculateProgressiveTax(resultatNet);
        assertEquals(new BigDecimal("90000.00"), tax);
    }

    @Test
    @DisplayName("Calcul de l'impôt progressif - Tranche 3 (> 1 000 000 DH)")
    void testTaxCalculation_Tranche3() {
        BigDecimal resultatNet = new BigDecimal("1500000.00");
        BigDecimal tax = declarationService.calculateProgressiveTax(resultatNet);
        assertEquals(new BigDecimal("340000.00"), tax);
    }

    @Test
    @DisplayName("Calcul de l'impôt progressif - Résultat nul ou négatif")
    void testTaxCalculation_ZeroOrNegative() {
        assertEquals(new BigDecimal("0.00"), declarationService.calculateProgressiveTax(new BigDecimal("-50000")));
        assertEquals(new BigDecimal("0.00"), declarationService.calculateProgressiveTax(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Calcul de la pénalité de retard - Dépôt dans les temps (<= 3 mois)")
    void testPenalty_OnTime() {
        LocalDate endExercice = LocalDate.of(2025, 12, 31);
        LocalDate dateDepot = LocalDate.of(2026, 3, 30);
        BigDecimal penalty = declarationService.calculatePenalty(new BigDecimal("10000.00"), endExercice, dateDepot);
        assertEquals(new BigDecimal("0.00"), penalty);
    }

    @Test
    @DisplayName("Calcul de la pénalité de retard - Dépôt en retard (> 3 mois)")
    void testPenalty_Late() {
        LocalDate endExercice = LocalDate.of(2025, 12, 31);
        LocalDate dateDepot = LocalDate.of(2026, 4, 15);
        BigDecimal penalty = declarationService.calculatePenalty(new BigDecimal("10000.00"), endExercice, dateDepot);
        assertEquals(new BigDecimal("1000.00"), penalty);
    }

    @Test
    @DisplayName("Création d'un brouillon de déclaration avec produits et charges")
    void testCreateDeclarationDraft() {
        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(mockUser));
        when(declarationRepository.save(any(Declaration.class))).thenAnswer(invocation -> {
            Declaration d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });

        DeclarationRequest request = new DeclarationRequest();
        request.setDateDebutExercice(LocalDate.of(2025, 1, 1));
        request.setDateFinExercice(LocalDate.of(2025, 12, 31));
        request.setProduits(List.of(new ProduitRequest("Ventes", new BigDecimal("200000"))));
        request.setCharges(List.of(new ChargeRequest("Achats", new BigDecimal("50000"))));

        DeclarationResponse response = declarationService.createDeclarationDraft(request, "12345678");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("200000"), response.getTotalProduits());
        assertEquals(new BigDecimal("50000"), response.getTotalCharges());
        assertEquals(new BigDecimal("150000"), response.getResultatNet());
        assertEquals(new BigDecimal("20000.00"), response.getMontantImpot());
        assertEquals(StatutDeclaration.BROUILLON.name(), response.getStatut());
    }

    @Test
    @DisplayName("Validation de la déclaration (Statut -> VALIDEE)")
    void testValidateDeclaration() {
        Declaration declaration = Declaration.builder()
                .id(15L)
                .utilisateur(mockUser)
                .statut(StatutDeclaration.BROUILLON)
                .dateDebutExercice(LocalDate.of(2025, 1, 1))
                .dateFinExercice(LocalDate.of(2025, 12, 31))
                .produits(new ArrayList<>())
                .charges(new ArrayList<>())
                .build();

        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(mockUser));
        when(declarationRepository.findById(15L)).thenReturn(Optional.of(declaration));
        when(declarationRepository.save(any(Declaration.class))).thenAnswer(inv -> inv.getArgument(0));

        DeclarationResponse res = declarationService.validateDeclaration(15L, null, "12345678");

        assertEquals(StatutDeclaration.VALIDEE.name(), res.getStatut());
    }

    @Test
    @DisplayName("Rectification d'une déclaration (Statut -> RECTIFIEE)")
    void testRectifyDeclaration() {
        Declaration declaration = Declaration.builder()
                .id(20L)
                .utilisateur(mockUser)
                .statut(StatutDeclaration.DEPOSEE)
                .dateDebutExercice(LocalDate.of(2025, 1, 1))
                .dateFinExercice(LocalDate.of(2025, 12, 31))
                .produits(new ArrayList<>())
                .charges(new ArrayList<>())
                .build();

        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(mockUser));
        when(declarationRepository.findById(20L)).thenReturn(Optional.of(declaration));
        when(declarationRepository.save(any(Declaration.class))).thenAnswer(inv -> inv.getArgument(0));

        DeclarationRequest request = new DeclarationRequest();
        request.setDateDebutExercice(LocalDate.of(2025, 1, 1));
        request.setDateFinExercice(LocalDate.of(2025, 12, 31));
        request.setProduits(List.of(new ProduitRequest("Ajustement Recettes", new BigDecimal("300000"))));

        DeclarationResponse res = declarationService.rectifyDeclaration(20L, request, "12345678");

        assertEquals(StatutDeclaration.RECTIFIEE.name(), res.getStatut());
        assertEquals(new BigDecimal("300000"), res.getTotalProduits());
    }

    @Test
    @DisplayName("Validation des dates d'exercice invalides (durée > 365 jours)")
    void testInvalidExerciceDates() {
        when(userRepository.findByNumeroFiscal("12345678")).thenReturn(Optional.of(mockUser));

        DeclarationRequest request = new DeclarationRequest();
        request.setDateDebutExercice(LocalDate.of(2025, 1, 1));
        request.setDateFinExercice(LocalDate.of(2026, 6, 1)); // > 365 jours

        assertThrows(BadRequestException.class, () -> declarationService.createDeclarationDraft(request, "12345678"));
    }
}
