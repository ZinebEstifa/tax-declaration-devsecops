package com.example.Declarations.fiscaux.controller;

import com.example.Declarations.fiscaux.dto.DeclarationRequest;
import com.example.Declarations.fiscaux.dto.DeclarationResponse;
import com.example.Declarations.fiscaux.service.DeclarationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/declarations")
public class DeclarationController {

    @Autowired
    private DeclarationService declarationService;

    @GetMapping
    public ResponseEntity<List<DeclarationResponse>> getAllDeclarations(Principal principal) {
        return ResponseEntity.ok(declarationService.getDeclarations(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeclarationResponse> getDeclarationById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(declarationService.getDeclaration(id, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<DeclarationResponse> createDraft(@Valid @RequestBody DeclarationRequest request, Principal principal) {
        DeclarationResponse response = declarationService.createDeclarationDraft(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeclarationResponse> updateDraft(@PathVariable Long id, @Valid @RequestBody DeclarationRequest request, Principal principal) {
        return ResponseEntity.ok(declarationService.updateDeclarationDraft(id, request, principal.getName()));
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<DeclarationResponse> validate(@PathVariable Long id, @RequestBody(required = false) DeclarationRequest request, Principal principal) {
        return ResponseEntity.ok(declarationService.validateDeclaration(id, request, principal.getName()));
    }

    @PostMapping("/{id}/rectifier")
    public ResponseEntity<DeclarationResponse> rectify(@PathVariable Long id, @Valid @RequestBody DeclarationRequest request, Principal principal) {
        return ResponseEntity.ok(declarationService.rectifyDeclaration(id, request, principal.getName()));
    }

    @PostMapping("/{id}/deposer")
    public ResponseEntity<DeclarationResponse> deposit(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(declarationService.depositDeclaration(id, principal.getName()));
    }
}
