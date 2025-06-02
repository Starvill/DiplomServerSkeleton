package com.example.product_service.repository;

import com.example.product_service.model.NutritionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionInfoRepository extends JpaRepository<NutritionInfo, Long> {
    void deleteByProductId(Long productId);
    List<NutritionInfo> findByProductId(Long productId);
}

