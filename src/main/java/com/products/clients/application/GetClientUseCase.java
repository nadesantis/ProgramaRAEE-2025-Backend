package com.products.clients.application;

import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetClientUseCase {

	@Autowired
    private ClientRepository repo;

    public GetClientUseCase(ClientRepository repo) { this.repo = repo; }

    public Optional<Client> handle(Long id) {
        return repo.findById(id);
    }
}
