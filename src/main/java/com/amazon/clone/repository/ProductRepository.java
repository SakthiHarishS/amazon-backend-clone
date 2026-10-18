package com.amazon.clone.repository;

import com.amazon.clone.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Category filter
    List<Product> findByCategoryIgnoreCase(String category);

    // Search product by keyword in name or description
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}