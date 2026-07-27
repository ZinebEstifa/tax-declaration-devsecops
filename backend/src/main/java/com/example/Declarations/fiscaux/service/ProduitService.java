package com.example.Declarations.fiscaux.service;

import com.example.Declarations.fiscaux.dto.ProduitRequest;
import com.example.Declarations.fiscaux.dto.ProduitResponse;
import com.example.Declarations.fiscaux.entity.Declaration;
import com.example.Declarations.fiscaux.entity.Produit;
import com.example.Declarations.fiscaux.exception.ResourceNotFoundException;
import com.example.Declarations.fiscaux.repository.DeclarationRepository;
import com.example.Declarations.fiscaux.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProduitService {

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private DeclarationRepository declarationRepository;

    @Autowired
    private DeclarationService declarationService;

    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsByDeclaration(Long declarationId, String numeroFiscal) {
        Declaration declaration = declarationService.getDeclarationEntity(declarationId, numeroFiscal);
        return declaration.getProduits().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProduitResponse addProduit(Long declarationId, ProduitRequest request, String numeroFiscal) {
        Declaration declaration = declarationService.getDeclarationEntity(declarationId, numeroFiscal);

        Produit produit = Produit.builder()
                .declaration(declaration)
                .libelle(request.getLibelle())
                .montant(request.getMontant())
                .build();

        Produit saved = produitRepository.save(produit);
        declaration.getProduits().add(saved);

        declarationService.recalculateTotals(declaration);
        declarationRepository.save(declaration);

        return mapToResponse(saved);
    }

    @Transactional
    public ProduitResponse updateProduit(Long produitId, ProduitRequest request, String numeroFiscal) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé."));

        Declaration declaration = produit.getDeclaration();
        declarationService.getDeclarationEntity(declaration.getId(), numeroFiscal);

        produit.setLibelle(request.getLibelle());
        produit.setMontant(request.getMontant());

        Produit saved = produitRepository.save(produit);

        declarationService.recalculateTotals(declaration);
        declarationRepository.save(declaration);

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteProduit(Long produitId, String numeroFiscal) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé."));

        Declaration declaration = produit.getDeclaration();
        declarationService.getDeclarationEntity(declaration.getId(), numeroFiscal);

        declaration.getProduits().remove(produit);
        produitRepository.delete(produit);

        declarationService.recalculateTotals(declaration);
        declarationRepository.save(declaration);
    }

    private ProduitResponse mapToResponse(Produit produit) {
        return ProduitResponse.builder()
                .id(produit.getId())
                .libelle(produit.getLibelle())
                .montant(produit.getMontant())
                .build();
    }
}
