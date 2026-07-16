package com.fitmind.ai.tool;

import com.fitmind.dto.nutrition.FoodDatabaseItem;
import com.fitmind.service.LocalFoodDatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchFoodsTool implements AITool {

    private final LocalFoodDatabaseService foodDbService;

    @Override
    public String getName() { return "search_foods"; }

    @Override
    public String getDescription() {
        return "Search the food database for foods matching a query. Supports filtering by nutrient (e.g. high protein, low calorie).";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("query", "Search term for food name");
        p.put("minProtein", "Minimum protein per 100g filter");
        p.put("maxCalories", "Maximum calories per 100g filter");
        p.put("minFiber", "Minimum fiber per 100g filter");
        p.put("maxSodium", "Maximum sodium per 100g filter");
        p.put("nutrient", "Specific nutrient to highlight (e.g. magnesium, iron, potassium, vitaminD)");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        String query = params.getOrDefault("query", "");
        List<FoodDatabaseItem> results = foodDbService.searchFoods(query, 15);

        if (results.isEmpty()) {
            return "No foods found matching '" + query + "'. Try a different search term.";
        }

        double minProtein = parseDouble(params.get("minProtein"), 0);
        double maxCalories = parseDouble(params.get("maxCalories"), 9999);
        double minFiber = parseDouble(params.get("minFiber"), 0);
        double maxSodium = parseDouble(params.get("maxSodium"), 9999);
        String highlightNutrient = params.get("nutrient");

        List<FoodDatabaseItem> filtered = results.stream()
                .filter(f -> f.getProteinPer100g() >= minProtein)
                .filter(f -> f.getCaloriesPer100g() <= maxCalories)
                .filter(f -> f.getFiberPer100g() >= minFiber)
                .filter(f -> f.getSodiumPer100g() == null || f.getSodiumPer100g() <= maxSodium)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            filtered = results;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(filtered.size()).append(" foods matching your criteria:\n\n");
        for (FoodDatabaseItem f : filtered) {
            String line = String.format("- %s: %.0f cal, %.1fg protein, %.1fg carbs, %.1fg fat, %.1fg fiber",
                    f.getName(), f.getCaloriesPer100g(), f.getProteinPer100g(),
                    f.getCarbsPer100g(), f.getFatPer100g(), f.getFiberPer100g());
            sb.append(line);

            if (highlightNutrient != null) {
                Double val = getNutrientValue(f, highlightNutrient);
                if (val != null && val > 0) {
                    sb.append(" (").append(highlightNutrient).append(": ").append(String.format("%.1f", val)).append(")");
                }
            }

            if (f.getProteinPer100g() >= 20) sb.append(" [HIGH PROTEIN]");
            if (f.getFiberPer100g() >= 5) sb.append(" [HIGH FIBER]");
            if (f.getCaloriesPer100g() <= 50) sb.append(" [LOW CALORIE]");
            sb.append("\n");
        }

        sb.append("\nNutrition values are per 100g. Adjust serving size for accurate intake.");
        return sb.toString();
    }

    private Double getNutrientValue(FoodDatabaseItem f, String nutrient) {
        return switch (nutrient.toLowerCase()) {
            case "magnesium" -> f.getMagnesiumPer100g();
            case "iron" -> f.getIronPer100g();
            case "potassium" -> f.getPotassiumPer100g();
            case "vitamind" -> f.getVitaminDPer100g();
            case "calcium" -> f.getCalciumPer100g();
            case "zinc" -> f.getZincPer100g();
            case "vitaminc" -> f.getVitaminCPer100g();
            case "vitaminb12" -> f.getVitaminB12Per100g();
            case "sodium" -> f.getSodiumPer100g();
            default -> null;
        };
    }

    private double parseDouble(String val, double defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
