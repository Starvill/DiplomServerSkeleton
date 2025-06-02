package com.example.product_service.service;
import com.example.product_service.model.ProductsNutritionFromJson;
import com.example.product_service.repository.ProductsNutritionFromJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsNutritionFromJsonService {
    @Autowired
    private ProductsNutritionFromJsonRepository repository;

    public Page<ProductsNutritionFromJson> getAllSortedByPopularity(Pageable pageable) {
        return repository.findAllByOrderByPopularityDesc(pageable);
    }

    public ProductsNutritionFromJson getById(String id) {
        return repository.findById(id).orElse(null);
    }

    public Page<ProductsNutritionFromJson> searchByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<ProductsNutritionFromJson> searchByBrand(String brand, Pageable pageable) {
        return repository.findByBrandContainingIgnoreCase(brand, pageable);
    }
}
