package com.example.product_service.service;

import com.example.product_service.model.NutritionInfo;
import com.example.product_service.repository.NutritionInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NutritionInfoService {
    private final NutritionInfoRepository nutritionInfoRepository;

    public NutritionInfoService(NutritionInfoRepository nutritionInfoRepository) {
        this.nutritionInfoRepository = nutritionInfoRepository;
    }

    public NutritionInfo saveNutritionInfo(NutritionInfo nutritionInfo) {
        return nutritionInfoRepository.save(nutritionInfo);
    }

    public List<NutritionInfo> getAllNutritionInfo() {
        return nutritionInfoRepository.findAll();
    }
    public List<NutritionInfo> getAllNutritionInfoByProductId(Long productId) {
        return nutritionInfoRepository.findByProductId(productId);
    }

    public Optional<NutritionInfo> getNutritionInfoById(Long id) {
        return nutritionInfoRepository.findById(id);
    }

    public void deleteNutritionInfo(Long id) {
        nutritionInfoRepository.deleteById(id);
    }

    public void deleteByProductId(Long productId) {
        nutritionInfoRepository.deleteByProductId(productId);
    }
}

