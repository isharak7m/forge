package com.forge.dto.nutrition;

import java.util.Map;

public class MicronutrientTargets {

    private MicronutrientTargets() {}

    public static final Map<String, Double> RDA = Map.ofEntries(
        Map.entry("vitaminA", 900.0),
        Map.entry("vitaminC", 90.0),
        Map.entry("vitaminD", 20.0),
        Map.entry("vitaminE", 15.0),
        Map.entry("vitaminK", 120.0),
        Map.entry("vitaminB1", 1.2),
        Map.entry("vitaminB2", 1.3),
        Map.entry("vitaminB3", 16.0),
        Map.entry("vitaminB5", 5.0),
        Map.entry("vitaminB6", 1.3),
        Map.entry("vitaminB7", 30.0),
        Map.entry("vitaminB9", 400.0),
        Map.entry("vitaminB12", 2.4),
        Map.entry("calcium", 1000.0),
        Map.entry("iron", 8.0),
        Map.entry("magnesium", 420.0),
        Map.entry("potassium", 4700.0),
        Map.entry("sodium", 2300.0),
        Map.entry("zinc", 11.0),
        Map.entry("copper", 0.9),
        Map.entry("manganese", 2.3),
        Map.entry("selenium", 55.0),
        Map.entry("phosphorus", 700.0),
        Map.entry("iodine", 150.0),
        Map.entry("chromium", 35.0)
    );

    public static double getTarget(String key) {
        return RDA.getOrDefault(key, 0.0);
    }

    public static String getUnit(String key) {
        return switch (key) {
            case "vitaminA" -> "mcg";
            case "vitaminD" -> "mcg";
            case "vitaminK" -> "mcg";
            case "vitaminB7" -> "mcg";
            case "vitaminB9" -> "mcg";
            case "vitaminB12" -> "mcg";
            case "calcium" -> "mg";
            case "iron" -> "mg";
            case "magnesium" -> "mg";
            case "potassium" -> "mg";
            case "sodium" -> "mg";
            case "zinc" -> "mg";
            case "copper" -> "mg";
            case "manganese" -> "mg";
            case "selenium" -> "mcg";
            case "phosphorus" -> "mg";
            case "iodine" -> "mcg";
            case "chromium" -> "mcg";
            default -> "mg";
        };
    }

    public static String getDisplayName(String key) {
        return switch (key) {
            case "vitaminA" -> "Vitamin A";
            case "vitaminC" -> "Vitamin C";
            case "vitaminD" -> "Vitamin D";
            case "vitaminE" -> "Vitamin E";
            case "vitaminK" -> "Vitamin K";
            case "vitaminB1" -> "Vitamin B1";
            case "vitaminB2" -> "Vitamin B2";
            case "vitaminB3" -> "Vitamin B3";
            case "vitaminB5" -> "Vitamin B5";
            case "vitaminB6" -> "Vitamin B6";
            case "vitaminB7" -> "Vitamin B7";
            case "vitaminB9" -> "Vitamin B9";
            case "vitaminB12" -> "Vitamin B12";
            case "calcium" -> "Calcium";
            case "iron" -> "Iron";
            case "magnesium" -> "Magnesium";
            case "potassium" -> "Potassium";
            case "sodium" -> "Sodium";
            case "zinc" -> "Zinc";
            case "copper" -> "Copper";
            case "manganese" -> "Manganese";
            case "selenium" -> "Selenium";
            case "phosphorus" -> "Phosphorus";
            case "iodine" -> "Iodine";
            case "chromium" -> "Chromium";
            default -> key;
        };
    }
}
