package com.example.product_service.model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "productsnutritionfromjson")
public class ProductsNutritionFromJson {
    @Id
    private String id;
    @Column(columnDefinition = "TEXT")
    private String name;
    @Column(columnDefinition = "TEXT")
    private String brand;
    @Column(columnDefinition = "TEXT")
    private String country;
    @Column(columnDefinition = "TEXT")
    private String quantity;

    @Column(columnDefinition = "text[]")
    private String[] allergens;

    @Column(columnDefinition = "TEXT")
    private String category;

    @Column(columnDefinition = "text[]")
    private String[] labels;

    private String nutriscore;

    @Column(name = "product_group")
    private String group;

    private Integer energyKcal;
    private Float fat;
    private Float saturatedFat;
    private Float carbohydrates;
    private Float sugars;
    private Float proteins;
    private Float salt;
    private Long popularity;

}
