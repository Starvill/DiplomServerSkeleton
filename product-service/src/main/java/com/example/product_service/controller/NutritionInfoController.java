package com.example.product_service.controller;

import com.example.product_service.model.NutritionInfo;
import com.example.product_service.service.NutritionInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products/nutrition")
public class NutritionInfoController {
    private final NutritionInfoService nutritionInfoService;

    public NutritionInfoController(NutritionInfoService nutritionInfoService) {
        this.nutritionInfoService = nutritionInfoService;
    }

    @PostMapping
    public ResponseEntity<NutritionInfo> createNutritionInfo(@RequestBody NutritionInfo nutritionInfo) {
        return ResponseEntity.ok(nutritionInfoService.saveNutritionInfo(nutritionInfo));
    }

    @GetMapping
    public ResponseEntity<List<NutritionInfo>> getAllNutritionInfo() {
        return ResponseEntity.ok(nutritionInfoService.getAllNutritionInfo());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<List<NutritionInfo>> getAllNutritionInfoByProductId(@PathVariable Long id) {
        return ResponseEntity.ok(nutritionInfoService.getAllNutritionInfoByProductId(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutritionInfo> getNutritionInfoById(@PathVariable Long id) {
        Optional<NutritionInfo> nutritionInfo = nutritionInfoService.getNutritionInfoById(id);
        return nutritionInfo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNutritionInfo(@PathVariable Long id) {
        nutritionInfoService.deleteNutritionInfo(id);
        return ResponseEntity.noContent().build();
    }
}
