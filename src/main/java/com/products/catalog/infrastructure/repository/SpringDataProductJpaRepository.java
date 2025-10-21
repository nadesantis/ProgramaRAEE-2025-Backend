package com.products.catalog.infrastructure.repository;

import com.products.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductJpaRepository extends JpaRepository<Product, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByActive(boolean active, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActive(String name, boolean active, Pageable pageable);
}
