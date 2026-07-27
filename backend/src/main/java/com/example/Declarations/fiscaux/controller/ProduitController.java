package com.example.Declarations.fiscaux.controller;

import com.example.Declarations.fiscaux.dto.ProduitRequest;
import com.example.Declarations.fiscaux.dto.ProduitResponse;
import com.example.Declarations.fiscaux.service.ProduitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping
public class ProduitController {

    @Autowired
    private ProduitService produitService;

    @GetMapping("/api/declarations/{declarationId}/produits")
    public ResponseEntity<List<ProduitResponse>> getProduitsByDeclaration(@PathVariable Long declarationId, Principal principal) {
        return ResponseEntity.ok(produitService.getProduitsByDeclaration(declarationId, principal.getName()));
    }

    @PostMapping("/api/declarations/{declarationId}/produits")
    public ResponseEntity<ProduitResponse> addProduit(@PathVariable Long declarationId,
                                                      @Valid @RequestBody ProduitRequest request,
                                                      Principal principal) {
        ProduitResponse response = produitService.addProduit(declarationId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/produits/{id}")
    public ResponseEntity<ProduitResponse> updateProduit(@PathVariable Long id,
                                                        @Valid @RequestBody ProduitRequest request,
                                                        Principal principal) {
        return ResponseEntity.ok(produitService.updateProduit(id, request, principal.getName()));
    }

    @DeleteMapping("/api/produits/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id, Principal principal) {
        produitService.deleteProduit(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
