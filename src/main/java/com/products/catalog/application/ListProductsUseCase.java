package com.products.catalog.application;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListProductsUseCase {

	@Autowired
    private ProductRepository repo;

    public ListProductsUseCase(ProductRepository repo) { this.repo = repo; }

    public Page<Product> handle(String name, Boolean active, Pageable pageable) {
        if (name != null && !name.isBlank() && active != null) {
            return repo.findByNameAndActive(name.trim(), active, pageable);
        } else if (name != null && !name.isBlank()) {
            return repo.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else if (active != null) {
            return repo.findByActive(active, pageable);
        }
        return repo.findAll(pageable);
    }
}
