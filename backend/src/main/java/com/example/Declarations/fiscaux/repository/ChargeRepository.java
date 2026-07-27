package com.example.Declarations.fiscaux.repository;

import com.example.Declarations.fiscaux.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {
    List<Charge> findByDeclarationId(Long declarationId);
}
