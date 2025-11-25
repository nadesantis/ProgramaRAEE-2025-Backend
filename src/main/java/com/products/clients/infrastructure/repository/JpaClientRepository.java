// src/main/java/com/products/clients/infrastructure/repository/JpaClientRepository.java
package com.products.clients.infrastructure.repository;

import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaClientRepository implements ClientRepository {

    private final SpringDataClientJpaRepository spring;

    public JpaClientRepository(SpringDataClientJpaRepository spring) {
        this.spring = spring;
    }

    @Override
    public Client save(Client client) { return spring.save(client); }

    @Override
    public Optional<Client> findById(Long id) { return spring.findById(id); }

    @Override
    public void deleteById(Long id) { spring.deleteById(id); }

    @Override
    public boolean existsByEmailIgnoreCase(String email) { return spring.existsByEmailIgnoreCase(email); }

    @Override
    public boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id) {
        return spring.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public boolean existsByTaxIdIgnoreCase(String taxId) { return spring.existsByTaxIdIgnoreCase(taxId); }

    @Override
    public boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id) {
        return spring.existsByTaxIdIgnoreCaseAndIdNot(taxId, id);
    }

    @Override
    public Page<Client> findAll(Pageable pageable) { return spring.findAll(pageable); }

    @Override
    public Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return spring.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public List<Client> findByNameContainingIgnoreCaseOrTaxIdContainingIgnoreCase(String name, String taxId) {
        return spring.findByNameContainingIgnoreCaseOrTaxIdContainingIgnoreCase(name, taxId);
    }

    // 👇 Este es el que te faltaba
    @Override
    public List<Client> findAll() {
        return spring.findAll();
    }
}
