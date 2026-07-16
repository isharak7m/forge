package com.fitmind.ai.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ToolOrchestrator {

    private final ToolRegistry registry;
    private final List<AITool> allTools;

    public ToolResult executeQuery(Long userId, String query) {
        String lower = query.toLowerCase();
        List<String> selectedTools = new ArrayList<>();

        if (lower.contains("food") || lower.contains("eat") || lower.contains("meal") || lower.contains("diet") || lower.contains("calorie") || lower.contains("protein") || lower.contains("nutrient")) {
            selectedTools.add("search_foods");
            selectedTools.add("analyze_nutrition");
        }
        if (lower.contains("weight") || lower.contains("predict") || lower.contains("future") || lower.contains("forecast") || lower.contains("goal")) {
            selectedTools.add("predict_weight");
            selectedTools.add("goal_simulation");
        }
        if (lower.contains("workout") || lower.contains("exercise") || lower.contains("bench") || lower.contains("squat") || lower.contains("deadlift") || lower.contains("lift") || lower.contains("train") || lower.contains("progressive") || lower.contains("overload")) {
            selectedTools.add("progressive_overload");
            selectedTools.add("exercise_analytics");
        }
        if (lower.contains("stronger") || lower.contains("progress") || lower.contains("improve") || lower.contains("pr") || lower.contains("personal record") || lower.contains("better")) {
            selectedTools.add("exercise_analytics");
            selectedTools.add("analyze_nutrition");
        }
        if (lower.contains("why") || lower.contains("stuck") || lower.contains("plateau") || lower.contains("reason")) {
            selectedTools.add("analyze_nutrition");
            selectedTools.add("exercise_analytics");
        }
        if (lower.contains("recovery") || lower.contains("rest") || lower.contains("muscle") || lower.contains("sore")) {
            selectedTools.add("muscle_recovery");
        }
        if (lower.contains("plan") || lower.contains("recommend") || lower.contains("should i")) {
            selectedTools.add("progressive_overload");
            selectedTools.add("meal_planner");
            selectedTools.add("goal_simulation");
        }
        if (lower.contains("report") || lower.contains("summary") || lower.contains("weekly") || lower.contains("overview")) {
            selectedTools.add("analyze_nutrition");
            selectedTools.add("exercise_analytics");
        }

        if (selectedTools.isEmpty()) {
            selectedTools.addAll(List.of("analyze_nutrition", "exercise_analytics", "predict_weight"));
        }

        selectedTools = selectedTools.stream().distinct().limit(3).collect(Collectors.toList());

        Map<String, String> results = new LinkedHashMap<>();
        for (String toolName : selectedTools) {
            AITool tool = registry.getTool(toolName);
            if (tool != null) {
                try {
                    Map<String, String> params = extractParams(query, tool);
                    String result = tool.execute(userId, params);
                    results.put(tool.getName(), result);
                } catch (Exception e) {
                    results.put(toolName, "Error: " + e.getMessage());
                }
            }
        }

        return new ToolResult(query, results, selectedTools);
    }

    private Map<String, String> extractParams(String query, AITool tool) {
        Map<String, String> params = new HashMap<>();
        String lower = query.toLowerCase();

        if (tool.getName().equals("search_foods")) {
            String foodQuery = query.replaceAll("(?i)(foods?|meals?|eat|ate|eaten|high in|rich in|with|under|over|low)", "").trim();
            if (foodQuery.length() > 3) params.put("query", foodQuery);
            if (lower.contains("high protein")) params.put("minProtein", "20");
            if (lower.contains("low calorie") || lower.contains("under") || lower.contains("under 300")) params.put("maxCalories", "300");
            if (lower.contains("high fiber")) params.put("minFiber", "5");
            if (lower.contains("low sodium")) params.put("maxSodium", "140");
            if (lower.contains("magnesium")) params.put("nutrient", "magnesium");
            if (lower.contains("vitamin d") || lower.contains("vitamin D")) params.put("nutrient", "vitaminD");
            if (lower.contains("potassium")) params.put("nutrient", "potassium");
            if (lower.contains("iron")) params.put("nutrient", "iron");
        }

        if (tool.getName().equals("progressive_overload")) {
            for (String ex : List.of("bench press", "squat", "deadlift", "overhead press", "barbell row", "pull up", "bicep curl", "tricep pushdown", "leg press", "lat pulldown", "shoulder press", "dumbbell curl")) {
                if (lower.contains(ex)) { params.put("exercise", ex); break; }
            }
        }

        if (tool.getName().equals("goal_simulation")) {
            if (lower.contains("calorie") || lower.contains("kcal")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*(calories?|kcal)").matcher(lower);
                if (m.find()) params.put("targetCalories", m.group(1));
            }
            if (lower.contains("protein")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*g\\s*protein").matcher(lower);
                if (m.find()) params.put("targetProtein", m.group(1));
            }
            if (lower.contains("week")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*weeks?").matcher(lower);
                if (m.find()) params.put("weeks", m.group(1));
            }
            if (lower.contains("day") && lower.contains("train")) params.put("trainingFrequency", "6");
            if (lower.contains("lose") && lower.contains("calorie")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*calories?").matcher(lower);
                if (m.find()) params.put("calorieDeficit", m.group(1));
            }
        }

        return params;
    }

    public record ToolResult(String query, Map<String, String> results, List<String> toolsUsed) {
        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("User query: ").append(query).append("\n\n");
            sb.append("Tools used: ").append(String.join(", ", toolsUsed)).append("\n\n");
            for (var e : results.entrySet()) {
                sb.append("=== ").append(e.getKey()).append(" ===\n");
                sb.append(e.getValue()).append("\n\n");
            }
            return sb.toString();
        }
    }
}
