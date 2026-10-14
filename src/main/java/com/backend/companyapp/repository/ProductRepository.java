package com.backend.companyapp.repository;

import com.backend.companyapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findFirstByNameIgnoreCase(String name);
    List<Product> findByNameContainingIgnoreCase(String name);
}
