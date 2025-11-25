package com.products.catalog.application;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CreateProductUseCase {

    private final ProductRepository repo;

    public CreateProductUseCase(ProductRepository repo) { this.repo = repo; }

    @Transactional
    public Product handle(String name, String description, BigDecimal unitPrice, Boolean active) {
        if (repo.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Product name already exists");
        }
        Product p = new Product();
        p.updateBasicInfo(name, description, unitPrice, active == null ? Boolean.TRUE : active);
        return repo.save(p);
    }
}
