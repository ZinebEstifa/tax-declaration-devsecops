package com.example.Declarations.fiscaux.repository;

import com.example.Declarations.fiscaux.entity.Declaration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeclarationRepository extends JpaRepository<Declaration, Long> {
    List<Declaration> findByUtilisateurId(Long utilisateurId);
}