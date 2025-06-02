package com.example.product_service.repository;
import com.example.product_service.model.ProductsNutritionFromJson;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProductsNutritionFromJsonRepository extends JpaRepository<ProductsNutritionFromJson, String> {
    Page<ProductsNutritionFromJson> findAllByOrderByPopularityDesc(Pageable pageable);

    Page<ProductsNutritionFromJson> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<ProductsNutritionFromJson> findByBrandContainingIgnoreCase(String brand, Pageable pageable);
}
