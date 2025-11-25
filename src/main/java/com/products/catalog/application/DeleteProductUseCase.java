package com.products.catalog.application;

import com.products.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteProductUseCase {
    private final ProductRepository repo;

    public DeleteProductUseCase(ProductRepository repo) { this.repo = repo; }

    @Transactional
    public void handle(Long id) {
        repo.deleteById(id);
    }
}
