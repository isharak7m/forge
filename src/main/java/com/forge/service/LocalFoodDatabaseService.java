package com.forge.service;

import com.forge.dto.nutrition.FoodDatabaseItem;
import com.forge.entity.FoodItem;
import com.forge.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocalFoodDatabaseService {

    private final FoodItemRepository foodItemRepository;

    public List<FoodDatabaseItem> searchFoods(String query) {
        return searchFoods(query, 50);
    }

    public List<FoodDatabaseItem> searchFoods(String query, int maxResults) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Page<FoodItem> results = foodItemRepository.searchByNamePaged(
                query.trim(), PageRequest.of(0, Math.min(maxResults, 100)));
        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public FoodDatabaseItem mapToDto(FoodItem f) {
        return FoodDatabaseItem.builder()
                .name(f.getName())
                .caloriesPer100g(f.getCaloriesPer100g() != null ? f.getCaloriesPer100g() : 0.0)
                .proteinPer100g(f.getProteinPer100g() != null ? f.getProteinPer100g() : 0.0)
                .carbsPer100g(f.getCarbsPer100g() != null ? f.getCarbsPer100g() : 0.0)
                .fatPer100g(f.getFatPer100g() != null ? f.getFatPer100g() : 0.0)
                .fiberPer100g(f.getFiberPer100g() != null ? f.getFiberPer100g() : 0.0)
                .vitaminAPer100g(f.getVitaminAPer100g())
                .vitaminCPer100g(f.getVitaminCPer100g())
                .vitaminDPer100g(f.getVitaminDPer100g())
                .vitaminEPer100g(f.getVitaminEPer100g())
                .vitaminKPer100g(f.getVitaminKPer100g())
                .vitaminB1Per100g(f.getVitaminB1Per100g())
                .vitaminB2Per100g(f.getVitaminB2Per100g())
                .vitaminB3Per100g(f.getVitaminB3Per100g())
                .vitaminB5Per100g(f.getVitaminB5Per100g())
                .vitaminB6Per100g(f.getVitaminB6Per100g())
                .vitaminB7Per100g(f.getVitaminB7Per100g())
                .vitaminB9Per100g(f.getVitaminB9Per100g())
                .vitaminB12Per100g(f.getVitaminB12Per100g())
                .calciumPer100g(f.getCalciumPer100g())
                .ironPer100g(f.getIronPer100g())
                .magnesiumPer100g(f.getMagnesiumPer100g())
                .potassiumPer100g(f.getPotassiumPer100g())
                .sodiumPer100g(f.getSodiumPer100g())
                .zincPer100g(f.getZincPer100g())
                .copperPer100g(f.getCopperPer100g())
                .manganesePer100g(f.getManganesePer100g())
                .seleniumPer100g(f.getSeleniumPer100g())
                .phosphorusPer100g(f.getPhosphorusPer100g())
                .iodinePer100g(f.getIodinePer100g())
                .chromiumPer100g(f.getChromiumPer100g())
                .build();
    }

    public boolean hasData() {
        return foodItemRepository.count() > 0;
    }
}
