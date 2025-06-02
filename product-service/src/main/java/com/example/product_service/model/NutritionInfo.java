package com.example.product_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "nutrition_info")
public class NutritionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long productDetailJsonId;

    @Column(nullable = false)
    private double grams;

    private String name;

    private double calories;
    private double fat;
    private double carbohydrates;
    private double protein;

    @Column(nullable = false)
    private Long  productId;
}