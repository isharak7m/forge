package com.forge.ai.tool;

import com.forge.dto.nutrition.FoodDatabaseItem;
import com.forge.entity.User;
import com.forge.entity.enums.FitnessGoal;
import com.forge.repository.UserRepository;
import com.forge.service.LocalFoodDatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MealPlannerTool implements AITool {

    private final LocalFoodDatabaseService foodDbService;
    private final UserRepository userRepository;

    private static final List<String> BREAKFAST_KEYS = List.of("oat", "egg", "yogurt", "milk", "banana", "cereal", "bread", "butter", "pancake", "waffle", "smoothie");
    private static final List<String> LUNCH_KEYS = List.of("chicken", "rice", "bread", "turkey", "salad", "tuna", "pasta", "wrap", "soup", "sandwich", "quinoa");
    private static final List<String> DINNER_KEYS = List.of("salmon", "beef", "chicken", "pasta", "rice", "potato", "broccoli", "steak", "shrimp", "tofu", "lamb", "pork", "fish");
    private static final List<String> SNACK_KEYS = List.of("almond", "peanut", "apple", "protein bar", "cheese", "cracker", "hummus", "carrot", "trail mix", "chocolate");

    @Override
    public String getName() { return "meal_planner"; }

    @Override
    public String getDescription() {
        return "Generate a meal plan suggestion based on available foods in the database, with options filtered by meal type.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("mealType", "Type of meal (breakfast, lunch, dinner, snack). Leave empty for full day plan.");
        p.put("goal", "Fitness goal (muscle_gain, weight_loss, general)");
        p.put("maxCalories", "Maximum calories per meal");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        String mealType = params.getOrDefault("mealType", "").toLowerCase();
        String goal = params.getOrDefault("goal", "general").toLowerCase();
        double maxCalPerMeal = parseDouble(params.get("maxCalories"), 800);

        User user = userRepository.findById(userId).orElse(null);
        double weight = user != null && user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 70.0;

        boolean muscleGain = goal.contains("muscle") || (user != null && user.getFitnessGoal() == FitnessGoal.MUSCLE_GAIN);
        boolean weightLoss = goal.contains("loss") || goal.contains("lose") ||
                (user != null && user.getFitnessGoal() == FitnessGoal.FAT_LOSS);

        double targetProteinPerMeal = muscleGain ? weight * 0.55 : weight * 0.4;

        Map<String, List<String>> mealStructure = new LinkedHashMap<>();
        if (mealType.isEmpty()) {
            mealStructure.put("Breakfast", BREAKFAST_KEYS);
            mealStructure.put("Lunch", LUNCH_KEYS);
            mealStructure.put("Dinner", DINNER_KEYS);
            mealStructure.put("Snack", SNACK_KEYS);
        } else if (mealType.contains("breakfast")) {
            mealStructure.put("Breakfast", BREAKFAST_KEYS);
        } else if (mealType.contains("lunch")) {
            mealStructure.put("Lunch", LUNCH_KEYS);
        } else if (mealType.contains("dinner")) {
            mealStructure.put("Dinner", DINNER_KEYS);
        } else if (mealType.contains("snack")) {
            mealStructure.put("Snack", SNACK_KEYS);
        } else {
            mealStructure.put("Meal", BREAKFAST_KEYS);
        }

        StringBuilder sb = new StringBuilder();
        if (mealType.isEmpty()) {
            sb.append("Suggested Daily Meal Plan:\n");
            if (muscleGain) sb.append("Goal: Muscle Gain — prioritize protein and calorie surplus\n");
            else if (weightLoss) sb.append("Goal: Weight Loss — prioritize protein, fiber, and controlled calories\n");
            else sb.append("Goal: General health — balanced macros\n\n");
        } else {
            sb.append("Suggested ").append(mealType).append(" options:\n\n");
        }

        for (var entry : mealStructure.entrySet()) {
            String mealName = entry.getKey();
            List<String> keywords = entry.getValue();

            sb.append("--- ").append(mealName).append(" ---\n");

            Set<String> usedFoods = new HashSet<>();
            int suggestions = 0;

            for (String keyword : keywords) {
                if (suggestions >= 3) break;
                List<FoodDatabaseItem> foods = foodDbService.searchFoods(keyword, 5);
                for (FoodDatabaseItem food : foods) {
                    if (suggestions >= 3) break;
                    if (usedFoods.contains(food.getName())) continue;
                    if (food.getCaloriesPer100g() > maxCalPerMeal) continue;
                    if (weightLoss && food.getFatPer100g() > 30) continue;
                    if (muscleGain && food.getProteinPer100g() < 5) continue;
                    if (food.getProteinPer100g() == 0 && food.getCaloriesPer100g() == 0) continue;

                    usedFoods.add(food.getName());
                    double protein = food.getProteinPer100g();
                    String proteinNote = (protein >= 20) ? " ★High protein" : "";
                    String fiberNote = (food.getFiberPer100g() >= 5) ? " ★High fiber" : "";
                    String lowCal = (food.getCaloriesPer100g() <= 50) ? " ★Low cal" : "";

                    String line = String.format("- %s: %.0f cal, %.1fg P, %.1fg C, %.1fg F%s%s%s",
                            food.getName(), food.getCaloriesPer100g(), food.getProteinPer100g(),
                            food.getCarbsPer100g(), food.getFatPer100g(), proteinNote, fiberNote, lowCal);
                    sb.append(line).append("\n");
                    suggestions++;
                }
            }

            if (suggestions == 0) {
                sb.append("- No specific suggestions found for ").append(mealName).append(" criteria. Try adjusting filters.\n");
            }
            sb.append("\n");
        }

        if (muscleGain) {
            sb.append("Tip: Pair these foods for balanced meals — e.g., chicken + rice + broccoli for lunch.\n");
            sb.append("Aim for ~").append(String.format("%.0f", targetProteinPerMeal)).append("g protein per meal.\n");
        } else if (weightLoss) {
            sb.append("Tip: Prioritize protein and fiber for satiety. Include vegetables at every meal.\n");
        }

        return sb.toString();
    }

    private double parseDouble(String val, double defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
