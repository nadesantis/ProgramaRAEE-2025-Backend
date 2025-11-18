// src/main/java/com/products/clients/infrastructure/repository/SpringDataClientJpaRepository.java
package com.products.clients.infrastructure.repository;

import com.products.clients.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataClientJpaRepository extends JpaRepository<Client, Long> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByTaxIdIgnoreCase(String taxId);
    boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

    // ✅ Paginados SIN EntityGraph (evita "firstResult/maxResults ... applying in memory")
    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Override
    Page<Client> findAll(Pageable pageable);

    List<Client> findByNameContainingIgnoreCaseOrTaxIdContainingIgnoreCase(String name, String taxId);

    // ✅ Detalle: si alguna vez necesitás las direcciones, las traés aquí
    @Override
    @EntityGraph(attributePaths = "addresses")
    Optional<Client> findById(Long id);
}
