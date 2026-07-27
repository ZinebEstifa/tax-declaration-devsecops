package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.dto.ChargeRequest;
import com.example.Declarations.fiscaux.dto.ChargeResponse;
import com.example.Declarations.fiscaux.dto.DeclarationRequest;
import com.example.Declarations.fiscaux.dto.DeclarationResponse;
import com.example.Declarations.fiscaux.dto.ProduitRequest;
import com.example.Declarations.fiscaux.dto.ProduitResponse;
import com.example.Declarations.fiscaux.entity.*;
import com.example.Declarations.fiscaux.exception.BadRequestException;
import com.example.Declarations.fiscaux.exception.ResourceNotFoundException;
import com.example.Declarations.fiscaux.repository.DeclarationRepository;
import com.example.Declarations.fiscaux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeclarationService {

    @Autowired
    private DeclarationRepository declarationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DeclarationResponse> getDeclarations(String numeroFiscal) {
        Utilisateur user = userRepository.findByNumeroFiscal(numeroFiscal)
                .orElseThrow(() -> new ResourceNotFoundException("Contribuable non trouvé."));

        return declarationRepository.findByUtilisateurId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeclarationResponse getDeclaration(Long id, String numeroFiscal) {
        Declaration declaration = getDeclarationEntity(id, numeroFiscal);
        return mapToResponse(declaration);
    }

    @Transactional
    public DeclarationResponse createDeclarationDraft(DeclarationRequest request, String numeroFiscal) {
        Utilisateur user = userRepository.findByNumeroFiscal(numeroFiscal)
                .orElseThrow(() -> new ResourceNotFoundException("Contribuable non trouvé."));

        validateExerciceDates(request.getDateDebutExercice(), request.getDateFinExercice());

        Declaration declaration = Declaration.builder()
                .utilisateur(user)
                .dateDebutExercice(request.getDateDebutExercice())
                .dateFinExercice(request.getDateFinExercice())
                .statut(StatutDeclaration.BROUILLON)
                .produits(new ArrayList<>())
                .charges(new ArrayList<>())
                .build();

        updateDeclarationData(declaration, request.getProduits(), request.getCharges());

        Declaration saved = declarationRepository.save(declaration);
        return mapToResponse(saved);
    }

    @Transactional
    public DeclarationResponse updateDeclarationDraft(Long id, DeclarationRequest request, String numeroFiscal) {
        Declaration declaration = getDeclarationEntity(id, numeroFiscal);

        validateExerciceDates(request.getDateDebutExercice(), request.getDateFinExercice());

        declaration.setDateDebutExercice(request.getDateDebutExercice());
        declaration.setDateFinExercice(request.getDateFinExercice());
        declaration.setStatut(StatutDeclaration.BROUILLON);

        updateDeclarationData(declaration, request.getProduits(), request.getCharges());

        Declaration saved = declarationRepository.save(declaration);
        return mapToResponse(saved);
    }

    @Transactional
    public DeclarationResponse validateDeclaration(Long id, DeclarationRequest request, String numeroFiscal) {
        Declaration declaration = getDeclarationEntity(id, numeroFiscal);

        if (request != null) {
            validateExerciceDates(request.getDateDebutExercice(), request.getDateFinExercice());
            declaration.setDateDebutExercice(request.getDateDebutExercice());
            declaration.setDateFinExercice(request.getDateFinExercice());
            updateDeclarationData(declaration, request.getProduits(), request.getCharges());
        }

        declaration.setStatut(StatutDeclaration.VALIDEE);
        recalculateTotals(declaration);

        Declaration saved = declarationRepository.save(declaration);
        return mapToResponse(saved);
    }

    @Transactional
    public DeclarationResponse rectifyDeclaration(Long id, DeclarationRequest request, String numeroFiscal) {
        Declaration declaration = getDeclarationEntity(id, numeroFiscal);

        validateExerciceDates(request.getDateDebutExercice(), request.getDateFinExercice());

        declaration.setDateDebutExercice(request.getDateDebutExercice());
        declaration.setDateFinExercice(request.getDateFinExercice());
        declaration.setStatut(StatutDeclaration.RECTIFIEE);

        if (declaration.getDateDepot() == null) {
            declaration.setDateDepot(LocalDate.now());
        }

        updateDeclarationData(declaration, request.getProduits(), request.getCharges());

        Declaration saved = declarationRepository.save(declaration);
        return mapToResponse(saved);
    }

    @Transactional
    public DeclarationResponse depositDeclaration(Long id, String numeroFiscal) {
        Declaration declaration = getDeclarationEntity(id, numeroFiscal);

        LocalDate today = LocalDate.now();
        declaration.setDateDepot(today);
        
        if (declaration.getStatut() == StatutDeclaration.DEPOSEE || declaration.getStatut() == StatutDeclaration.RECTIFIEE) {
            declaration.setStatut(StatutDeclaration.RECTIFIEE);
        } else {
            declaration.setStatut(StatutDeclaration.DEPOSEE);
        }

        recalculateTotals(declaration);

        Declaration saved = declarationRepository.save(declaration);
        return mapToResponse(saved);
    }

    public Declaration getDeclarationEntity(Long id, String numeroFiscal) {
        Utilisateur user = userRepository.findByNumeroFiscal(numeroFiscal)
                .orElseThrow(() -> new ResourceNotFoundException("Contribuable non trouvé."));

        Declaration declaration = declarationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Déclaration non trouvée."));

        if (!declaration.getUtilisateur().getId().equals(user.getId())) {
            throw new BadRequestException("Accès non autorisé à cette déclaration.");
        }

        return declaration;
    }

    private void validateExerciceDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BadRequestException("Les dates de début et de fin d'exercice sont obligatoires.");
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("La date de fin d'exercice doit être postérieure à la date de début.");
        }
        long days = ChronoUnit.DAYS.between(start, end);
        if (days > 365) {
            throw new BadRequestException("La durée de l'exercice fiscal ne doit pas dépasser 365 jours.");
        }
    }

    private void updateDeclarationData(Declaration declaration, List<ProduitRequest> produitRequests, List<ChargeRequest> chargeRequests) {
        declaration.getProduits().clear();
        declaration.getCharges().clear();

        if (produitRequests != null) {
            for (ProduitRequest req : produitRequests) {
                Produit produit = Produit.builder()
                        .declaration(declaration)
                        .libelle(req.getLibelle())
                        .montant(req.getMontant())
                        .build();
                declaration.getProduits().add(produit);
            }
        }

        if (chargeRequests != null) {
            for (ChargeRequest req : chargeRequests) {
                Charge charge = Charge.builder()
                        .declaration(declaration)
                        .libelle(req.getLibelle())
                        .montant(req.getMontant())
                        .build();
                declaration.getCharges().add(charge);
            }
        }

        recalculateTotals(declaration);
    }

    public void recalculateTotals(Declaration declaration) {
        BigDecimal totalProduits = declaration.getProduits().stream()
                .map(Produit::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCharges = declaration.getCharges().stream()
                .map(Charge::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal resultatNet = totalProduits.subtract(totalCharges);

        declaration.setTotalProduits(totalProduits);
        declaration.setTotalCharges(totalCharges);
        declaration.setResultatNet(resultatNet);

        BigDecimal impot = calculateProgressiveTax(resultatNet);
        declaration.setMontantImpot(impot);

        LocalDate dateDepot = declaration.getDateDepot() != null ? declaration.getDateDepot() : LocalDate.now();
        BigDecimal penalite = calculatePenalty(impot, declaration.getDateFinExercice(), dateDepot);
        declaration.setPenaliteRetard(penalite);
    }

    public BigDecimal calculateProgressiveTax(BigDecimal netResult) {
        if (netResult == null || netResult.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal tranche1Limit = new BigDecimal("100000");
        BigDecimal tranche2Limit = new BigDecimal("1000000");

        if (netResult.compareTo(tranche1Limit) <= 0) {
            tax = netResult.multiply(new BigDecimal("0.10"));
        } else if (netResult.compareTo(tranche2Limit) <= 0) {
            BigDecimal taxTranche1 = tranche1Limit.multiply(new BigDecimal("0.10"));
            BigDecimal rest = netResult.subtract(tranche1Limit);
            tax = taxTranche1.add(rest.multiply(new BigDecimal("0.20")));
        } else {
            BigDecimal taxTranche1 = tranche1Limit.multiply(new BigDecimal("0.10"));
            BigDecimal taxTranche2 = tranche2Limit.subtract(tranche1Limit).multiply(new BigDecimal("0.20"));
            BigDecimal rest = netResult.subtract(tranche2Limit);
            tax = taxTranche1.add(taxTranche2).add(rest.multiply(new BigDecimal("0.30")));
        }

        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePenalty(BigDecimal taxAmount, LocalDate dateFinExercice, LocalDate dateDepot) {
        if (taxAmount == null || taxAmount.compareTo(BigDecimal.ZERO) <= 0 || dateFinExercice == null || dateDepot == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        LocalDate dateLimite = dateFinExercice.plusMonths(3);
        if (dateDepot.isAfter(dateLimite)) {
            return taxAmount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public DeclarationResponse mapToResponse(Declaration dec) {
        List<ProduitResponse> prods = dec.getProduits().stream()
                .map(p -> new ProduitResponse(p.getId(), p.getLibelle(), p.getMontant()))
                .collect(Collectors.toList());

        List<ChargeResponse> chgs = dec.getCharges().stream()
                .map(c -> new ChargeResponse(c.getId(), c.getLibelle(), c.getMontant()))
                .collect(Collectors.toList());

        return DeclarationResponse.builder()
                .id(dec.getId())
                .dateDebutExercice(dec.getDateDebutExercice())
                .dateFinExercice(dec.getDateFinExercice())
                .dateDepot(dec.getDateDepot())
                .totalProduits(dec.getTotalProduits())
                .totalCharges(dec.getTotalCharges())
                .resultatNet(dec.getResultatNet())
                .montantImpot(dec.getMontantImpot())
                .penaliteRetard(dec.getPenaliteRetard())
                .statut(dec.getStatut().name())
                .produits(prods)
                .charges(chgs)
                .dateCreation(dec.getDateCreation())
                .dateModification(dec.getDateModification())
                .build();
    }
}
