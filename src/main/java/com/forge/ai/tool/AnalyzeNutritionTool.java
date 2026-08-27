package com.forge.ai.tool;

import com.forge.dto.nutrition.DailyNutritionSummary;
import com.forge.repository.FoodLogRepository;
import com.forge.repository.WeightLogRepository;
import com.forge.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalyzeNutritionTool implements AITool {

    private final NutritionService nutritionService;
    private final FoodLogRepository foodLogRepository;
    private final WeightLogRepository weightLogRepository;

    @Override
    public String getName() { return "analyze_nutrition"; }

    @Override
    public String getDescription() {
        return "Analyze daily nutrition including calories, macros, micronutrients, and dietary patterns over a date range.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("days", "Number of days to analyze (default: 7)");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        int days = parseInt(params.get("days"), 7);
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);

        long foodDaysLogged = foodLogRepository.countDistinctDatesLogged(userId, from, to);
        Double totalCal = foodLogRepository.sumCaloriesByUserIdAndDateBetween(userId, from, to);
        Double totalPro = foodLogRepository.sumProteinByUserIdAndDateBetween(userId, from, to);
        Double totalCarb = foodLogRepository.sumCarbsByUserIdAndDateBetween(userId, from, to);
        Double totalFat = foodLogRepository.sumFatByUserIdAndDateBetween(userId, from, to);
        Double avgWeight = weightLogRepository.avgWeightByUserIdAndDateBetween(userId, from, to);

        totalCal = totalCal != null ? totalCal : 0;
        totalPro = totalPro != null ? totalPro : 0;
        totalCarb = totalCarb != null ? totalCarb : 0;
        totalFat = totalFat != null ? totalFat : 0;

        int analyzedDays = Math.max((int) foodDaysLogged, 1);
        double avgCalDay = totalCal / analyzedDays;
        double avgProDay = totalPro / analyzedDays;
        double avgCarbDay = totalCarb / analyzedDays;
        double avgFatDay = totalFat / analyzedDays;

        double calFromPro = avgProDay * 4;
        double calFromCarb = avgCarbDay * 4;
        double calFromFat = avgFatDay * 9;
        double totalMacroCal = calFromPro + calFromCarb + calFromFat;

        double proPct = totalMacroCal > 0 ? (calFromPro / totalMacroCal) * 100 : 0;
        double carbPct = totalMacroCal > 0 ? (calFromCarb / totalMacroCal) * 100 : 0;
        double fatPct = totalMacroCal > 0 ? (calFromFat / totalMacroCal) * 100 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Nutrition Analysis (last ").append(days).append(" days):\n");
        sb.append("├─ Days with food logged: ").append(foodDaysLogged).append("/").append(days).append("\n");
        sb.append("├─ Avg daily calories: ").append(String.format("%.0f", avgCalDay)).append(" kcal\n");
        sb.append("├─ Avg protein: ").append(String.format("%.0f", avgProDay)).append("g/day\n");
        sb.append("├─ Avg carbs: ").append(String.format("%.0f", avgCarbDay)).append("g/day\n");
        sb.append("├─ Avg fat: ").append(String.format("%.0f", avgFatDay)).append("g/day\n");

        sb.append("\n").append("Macro split:\n");
        sb.append("├─ Protein: ").append(String.format("%.0f", proPct)).append("% (")
                .append(String.format("%.0f", calFromPro)).append(" cal)\n");
        sb.append("├─ Carbs: ").append(String.format("%.0f", carbPct)).append("% (")
                .append(String.format("%.0f", calFromCarb)).append(" cal)\n");
        sb.append("├─ Fat: ").append(String.format("%.0f", fatPct)).append("% (")
                .append(String.format("%.0f", calFromFat)).append(" cal)\n");

        sb.append("\n").append("Assessment:\n");
        if (foodDaysLogged < days * 0.5) {
            sb.append("├─ Inconsistent logging. Try to log daily for better tracking.\n");
        }
        if (avgProDay < 100) {
            sb.append("├─ Protein intake is low (").append(String.format("%.0f", avgProDay)).append("g). Aim for 1.6-2.2g/kg bodyweight.\n");
        } else if (avgProDay >= 150) {
            sb.append("├─ Protein intake is excellent (").append(String.format("%.0f", avgProDay)).append("g/day).\n");
        }
        if (proPct < 15) sb.append("├─ Protein only ").append(String.format("%.0f", proPct)).append("% of calories. Increase protein sources.\n");
        if (fatPct < 15) sb.append("├─ Fat intake very low (").append(String.format("%.0f", fatPct)).append("%). Essential for hormone function.\n");
        if (fatPct > 40) sb.append("├─ Fat intake high (").append(String.format("%.0f", fatPct)).append("%). Consider moderating.\n");
        if (avgCalDay < 1200) sb.append("├─ Very low calorie intake. Ensure you're eating enough for your activity level.\n");
        if (avgCalDay > 3000) sb.append("├─ High calorie intake. Ensure this aligns with your goals.\n");
        if (avgWeight != null) {
            sb.append("├─ Avg body weight this period: ").append(String.format("%.1f", avgWeight)).append(" kg\n");
        }

        List<DailyNutritionSummary> dailySummaries = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            if (!d.isAfter(to)) {
                dailySummaries.add(nutritionService.getDailyAnalytics(userId, d));
            }
        }
        List<DailyNutritionSummary> daysWithCalories = dailySummaries.stream()
                .filter(d -> d.getTotalCalories() > 0).collect(Collectors.toList());

        if (daysWithCalories.size() >= 3) {
            double calStdDev = calculateStdDev(daysWithCalories.stream()
                    .mapToDouble(DailyNutritionSummary::getTotalCalories).toArray());
            sb.append("\n").append("Consistency: ");
            if (calStdDev < 200) {
                sb.append("Very consistent (").append(String.format("%.0f", calStdDev)).append(" kcal std dev)");
            } else if (calStdDev < 400) {
                sb.append("Moderately consistent (").append(String.format("%.0f", calStdDev)).append(" kcal std dev)");
            } else {
                sb.append("Highly variable (").append(String.format("%.0f", calStdDev)).append(" kcal std dev). Consider meal prepping.");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private double calculateStdDev(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values).map(v -> Math.pow(v - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    private int parseInt(String val, int defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
