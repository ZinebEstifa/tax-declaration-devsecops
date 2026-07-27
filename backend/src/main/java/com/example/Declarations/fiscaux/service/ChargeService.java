package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.dto.ChargeRequest;
import com.example.Declarations.fiscaux.dto.ChargeResponse;
import com.example.Declarations.fiscaux.entity.Charge;
import com.example.Declarations.fiscaux.entity.Declaration;
import com.example.Declarations.fiscaux.exception.ResourceNotFoundException;
import com.example.Declarations.fiscaux.repository.ChargeRepository;
import com.example.Declarations.fiscaux.repository.DeclarationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeService {

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private DeclarationRepository declarationRepository;

    @Autowired
    private DeclarationService declarationService;

    @Transactional(readOnly = true)
    public List<ChargeResponse> getChargesByDeclaration(Long declarationId, String numeroFiscal) {
        Declaration declaration = declarationService.getDeclarationEntity(declarationId, numeroFiscal);
        return declaration.getCharges().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChargeResponse addCharge(Long declarationId, ChargeRequest request, String numeroFiscal) {
        Declaration declaration = declarationService.getDeclarationEntity(declarationId, numeroFiscal);

        Charge charge = Charge.builder()
                .declaration(declaration)
                .libelle(request.getLibelle())
                .montant(request.getMontant())
                .build();

        Charge saved = chargeRepository.save(charge);
        declaration.getCharges().add(saved);

        declarationService.recalculateTotals(declaration);
        declarationRepository.save(declaration);

        return mapToResponse(saved);
    }

    @Transactional
    public ChargeResponse updateCharge(Long chargeId, ChargeRequest request, String numeroFiscal) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Charge non trouvée."));

        Declaration declaration = charge.getDeclaration();
        declarationService.getDeclarationEntity(declaration.getId(), numeroFiscal);

        charge.setLibelle(request.getLibelle());
        charge.setMontant(request.getMontant());

        Charge saved = chargeRepository.save(charge);

        declarationService.recalculateTotals(declaration);
        declarationRepository.save(declaration);

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCharge(Long chargeId, String numeroFiscal) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Charge non trouvée."));

        Declaration declaration = charge.getDeclaration();
        declarationService.getDeclarationEntity(declaration.getId(), numeroFiscal);

        declaration.getCharges().remove(charge);
        chargeRepository.delete(charge);

        declarationService.recalculateTotals(declaration);
        declarationRepository.save(declaration);
    }

    private ChargeResponse mapToResponse(Charge charge) {
        return ChargeResponse.builder()
                .id(charge.getId())
                .libelle(charge.getLibelle())
                .montant(charge.getMontant())
                .build();
    }
}
