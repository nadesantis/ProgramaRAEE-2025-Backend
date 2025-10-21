package com.products.clients.application;

import com.products.clients.domain.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteClientUseCase {

    private final ClientRepository repo;

    public DeleteClientUseCase(ClientRepository repo) { this.repo = repo; }

    @Transactional
    public void handle(Long id) {
        repo.deleteById(id);
    }
}
