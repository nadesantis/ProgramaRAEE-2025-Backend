package com.products.clients.application;

import com.products.clients.domain.Address;
import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreateClientUseCase {

	@Autowired
    private ClientRepository repo;

    public CreateClientUseCase(ClientRepository repo) { this.repo = repo; }

    @Transactional
    public Client handle(String name, String email, String phone, String taxId, List<Address> addresses) {

        if (email != null && repo.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (taxId != null && repo.existsByTaxIdIgnoreCase(taxId)) {
            throw new IllegalArgumentException("Tax ID already exists");
        }

        Client c = new Client();
        c.updateBasicInfo(name, email, phone, taxId);
        c.replaceAddresses(addresses);
        return repo.save(c);
    }
}
