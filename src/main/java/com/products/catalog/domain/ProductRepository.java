// src/main/java/com/products/catalog/domain/ProductRepository.java
package com.products.catalog.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    void deleteById(Long id);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // 🔹 Paginado (UI)
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByActive(boolean active, Pageable pageable);
    Page<Product> findByNameAndActive(String name, boolean active, Pageable pageable);

    // 🔹 **Overload para exportación** (sin Pageable)
    List<Product> findAll();
}
