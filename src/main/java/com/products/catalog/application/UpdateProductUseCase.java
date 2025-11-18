package com.products.catalog.application;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UpdateProductUseCase {

	@Autowired
    private ProductRepository repo;

    public UpdateProductUseCase(ProductRepository repo) { this.repo = repo; }

    @Transactional
    public Product handle(Long id, String name, String description, BigDecimal unitPrice, Boolean active) {
        Product p = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (name != null && repo.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Product name already exists");
        }

        p.updateBasicInfo(name, description, unitPrice, active);
        return repo.save(p);
    }
}
