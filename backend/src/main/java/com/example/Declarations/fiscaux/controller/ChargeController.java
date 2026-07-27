package com.example.Declarations.fiscaux.controller;

import com.example.Declarations.fiscaux.dto.ChargeRequest;
import com.example.Declarations.fiscaux.dto.ChargeResponse;
import com.example.Declarations.fiscaux.service.ChargeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping
public class ChargeController {

    @Autowired
    private ChargeService chargeService;

    @GetMapping("/api/declarations/{declarationId}/charges")
    public ResponseEntity<List<ChargeResponse>> getChargesByDeclaration(@PathVariable Long declarationId, Principal principal) {
        return ResponseEntity.ok(chargeService.getChargesByDeclaration(declarationId, principal.getName()));
    }

    @PostMapping("/api/declarations/{declarationId}/charges")
    public ResponseEntity<ChargeResponse> addCharge(@PathVariable Long declarationId,
                                                   @Valid @RequestBody ChargeRequest request,
                                                   Principal principal) {
        ChargeResponse response = chargeService.addCharge(declarationId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/charges/{id}")
    public ResponseEntity<ChargeResponse> updateCharge(@PathVariable Long id,
                                                     @Valid @RequestBody ChargeRequest request,
                                                     Principal principal) {
        return ResponseEntity.ok(chargeService.updateCharge(id, request, principal.getName()));
    }

    @DeleteMapping("/api/charges/{id}")
    public ResponseEntity<Void> deleteCharge(@PathVariable Long id, Principal principal) {
        chargeService.deleteCharge(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
