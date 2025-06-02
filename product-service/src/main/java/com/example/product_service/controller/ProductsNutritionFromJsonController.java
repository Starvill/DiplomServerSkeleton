package com.example.product_service.controller;
import com.example.product_service.model.ProductsNutritionFromJson;
import com.example.product_service.service.ProductsNutritionFromJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/products/nutritionjson")
public class ProductsNutritionFromJsonController {

    @Autowired
    private ProductsNutritionFromJsonService service;

    // 1. Вывод по 20 строк с сортировкой по популярности
    @GetMapping("/popular")
    public Page<ProductsNutritionFromJson> getPopularProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getAllSortedByPopularity(pageable);
    }

    // 2. Поиск по ID
    @GetMapping("/{id}")
    public ProductsNutritionFromJson getProductById(@PathVariable String id) {
        return service.getById(id);
    }

    // 3. Поиск по имени с пагинацией
    @GetMapping("/search/name")
    public Page<ProductsNutritionFromJson> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.searchByName(name, pageable);
    }

    // 4. Поиск по бренду с пагинацией
    @GetMapping("/search/brand")
    public Page<ProductsNutritionFromJson> searchByBrand(
            @RequestParam String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.searchByBrand(brand, pageable);
    }
}
