package com.example.Declarations.fiscaux.repository;

import com.example.Declarations.fiscaux.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByNumeroFiscal(String numeroFiscal);
}
