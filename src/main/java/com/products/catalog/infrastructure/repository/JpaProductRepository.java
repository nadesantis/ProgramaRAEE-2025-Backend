package com.products.catalog.infrastructure.repository;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaProductRepository implements ProductRepository {

	@Autowired
    private SpringDataProductJpaRepository spring;

    public JpaProductRepository(SpringDataProductJpaRepository spring) {
        this.spring = spring;
    }

    @Override
    public Product save(Product product) { return spring.save(product); }

    @Override
    public Optional<Product> findById(Long id) { return spring.findById(id); }

    @Override
    public void deleteById(Long id) { spring.deleteById(id); }

    @Override
    public boolean existsByNameIgnoreCase(String name) { return spring.existsByNameIgnoreCase(name); }

    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return spring.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) { return spring.findAll(pageable); }

    @Override
    public Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return spring.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> findByActive(boolean active, Pageable pageable) {
        return spring.findByActive(active, pageable);
    }

    @Override
    public Page<Product> findByNameAndActive(String name, boolean active, Pageable pageable) {
        return spring.findByNameContainingIgnoreCaseAndActive(name, active, pageable);
    }

	@Override
	public List<Product> findAll() {
		return spring.findAll();
	}
}
