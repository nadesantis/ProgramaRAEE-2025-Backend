package com.products.catalog.application;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GetProductUseCase {

	@Autowired
    private ProductRepository repo;

    public GetProductUseCase(ProductRepository repo) { this.repo = repo; }

    public Optional<Product> handle(Long id) {
        return repo.findById(id);
    }
}
