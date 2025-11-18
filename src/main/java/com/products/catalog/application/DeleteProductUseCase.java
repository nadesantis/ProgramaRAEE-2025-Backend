package com.products.catalog.application;

import com.products.catalog.domain.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteProductUseCase {
	
	@Autowired
    private  ProductRepository repo;

    public DeleteProductUseCase(ProductRepository repo) { this.repo = repo; }

    @Transactional
    public void handle(Long id) {
        repo.deleteById(id);
    }
}
