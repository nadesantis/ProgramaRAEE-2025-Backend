package com.products.clients.application;

import com.products.clients.domain.Address;
import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UpdateClientUseCase {

    private final ClientRepository repo;

    public UpdateClientUseCase(ClientRepository repo) { this.repo = repo; }

    @Transactional
    public Client handle(Long id, String name, String email, String phone, String taxId, List<Address> addresses) {
        Client c = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (email != null && repo.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (taxId != null && repo.existsByTaxIdIgnoreCaseAndIdNot(taxId, id)) {
            throw new IllegalArgumentException("Tax ID already exists");
        }

        c.updateBasicInfo(name, email, phone, taxId);
        if (addresses != null) {
            c.replaceAddresses(addresses);
        }
        return repo.save(c);
    }
}
