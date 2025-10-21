package com.products.clients.application;

import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListClientsUseCase {

    private final ClientRepository repo;

    public ListClientsUseCase(ClientRepository repo) { this.repo = repo; }

    public Page<Client> handle(String name, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return repo.findByNameContainingIgnoreCase(name.trim(), pageable);
        }
        return repo.findAll(pageable);
    }
}
