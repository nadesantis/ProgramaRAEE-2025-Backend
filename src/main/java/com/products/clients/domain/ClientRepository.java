package com.products.clients.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(Long id);
    void deleteById(Long id);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByTaxIdIgnoreCase(String taxId);
    boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

    Page<Client> findAll(Pageable pageable);
    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
