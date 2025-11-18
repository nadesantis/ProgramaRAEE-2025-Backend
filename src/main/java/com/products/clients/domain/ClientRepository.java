// src/main/java/com/products/clients/domain/ClientRepository.java
package com.products.clients.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(Long id);
    void deleteById(Long id);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByTaxIdIgnoreCase(String taxId);
    boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

    // 🔹 Paginado (lo usas en pantallas)
    Page<Client> findAll(Pageable pageable);
    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 🔹 Para búsquedas simples (autocompletes, etc.)
    List<Client> findByNameContainingIgnoreCaseOrTaxIdContainingIgnoreCase(String name, String taxId);

    // 🔹 **Overload para exportación** (sin Pageable)
    List<Client> findAll();
}
