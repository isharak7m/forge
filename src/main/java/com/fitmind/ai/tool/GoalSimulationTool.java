package com.fitmind.ai.tool;

import com.fitmind.entity.ExerciseLog;
import com.fitmind.entity.User;
import com.fitmind.entity.enums.FitnessGoal;
import com.fitmind.repository.ExerciseLogRepository;
import com.fitmind.repository.FoodLogRepository;
import com.fitmind.repository.UserRepository;
import com.fitmind.repository.WeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class GoalSimulationTool implements AITool {

    private final UserRepository userRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final FoodLogRepository foodLogRepository;
    private final WeightLogRepository weightLogRepository;

    @Override
    public String getName() { return "goal_simulation"; }

    @Override
    public String getDescription() {
        return "Simulate the outcome of a fitness goal. Project weight change or strength gain based on current habits and targets.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("targetCalories", "Daily calorie target");
        p.put("targetProtein", "Daily protein target in grams");
        p.put("trainingFrequency", "Days per week of training");
        p.put("weeks", "Number of weeks to simulate (default: 12)");
        p.put("calorieDeficit", "Daily calorie deficit for weight loss simulation");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        User user = userRepository.findById(userId).orElse(null);
        int weeks = parseInt(params.get("weeks"), 12);

        double currentWeight = user != null && user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 70.0;
        int trainingFreq = parseInt(params.get("trainingFrequency"), 4);
        int calorieDeficit = parseInt(params.get("calorieDeficit"), 0);
        int targetCalories = parseInt(params.get("targetCalories"), 0);
        int targetProtein = parseInt(params.get("targetProtein"), 0);

        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();
        Double recentCalories = foodLogRepository.sumCaloriesByUserIdAndDateBetween(userId, from, to);
        Double recentProtein = foodLogRepository.sumProteinByUserIdAndDateBetween(userId, from, to);
        long daysLogged = foodLogRepository.countDistinctDatesLogged(userId, from, to);
        int activeDays = Math.max((int) daysLogged, 1);

        double currentAvgCal = recentCalories != null ? recentCalories / activeDays : 2000.0;
        double currentAvgPro = recentProtein != null ? recentProtein / activeDays : 80.0;

        int totalSessions = (int) exerciseLogRepository.findAllByUserId(userId).stream()
                .map(l -> l.getWorkoutSession().getId()).distinct().count();

        double weeklyCaloricDeficit;
        if (calorieDeficit > 0) {
            weeklyCaloricDeficit = calorieDeficit * 7.0;
        } else if (targetCalories > 0) {
            weeklyCaloricDeficit = (currentAvgCal - targetCalories) * 7.0;
        } else {
            weeklyCaloricDeficit = 0;
        }

        double weeklyWeightChangeKg = weeklyCaloricDeficit / 7700.0;
        double projectedWeight = currentWeight + (weeklyWeightChangeKg * weeks);
        boolean isWeightLoss = weeklyWeightChangeKg < 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Goal Simulation (").append(weeks).append(" weeks):\n\n");

        sb.append("Current status:\n");
        sb.append("├─ Weight: ").append(String.format("%.1f", currentWeight)).append(" kg\n");
        sb.append("├─ Avg daily calories: ").append(String.format("%.0f", currentAvgCal)).append(" kcal\n");
        sb.append("├─ Avg daily protein: ").append(String.format("%.0f", currentAvgPro)).append("g\n");
        sb.append("├─ Training frequency: ~").append(trainingFreq).append(" days/week (")
                .append(totalSessions).append(" total sessions all time)\n");

        sb.append("\n").append("Projected outcome:\n");
        if (isWeightLoss) {
            double totalLoss = currentWeight - projectedWeight;
            sb.append("├─ Projected weight: ").append(String.format("%.1f", projectedWeight)).append(" kg\n");
            sb.append("├─ Total loss: ").append(String.format("%.1f", totalLoss)).append(" kg over ").append(weeks).append(" weeks\n");
            sb.append("├─ Weekly rate: ").append(String.format("%.2f", Math.abs(weeklyWeightChangeKg))).append(" kg/week");
            double rate = Math.abs(weeklyWeightChangeKg);
            if (rate < 0.25) sb.append(" (very gradual, may be hard to sustain deficit)");
            else if (rate <= 0.5) sb.append(" (ideal sustainable rate of 0.25-0.5 kg/week)");
            else if (rate <= 1.0) sb.append(" (aggressive — may lose muscle if protein is insufficient)");
            else sb.append(" (very aggressive — not recommended without supervision)");
            sb.append("\n");
        } else if (weeklyWeightChangeKg > 0) {
            sb.append("├─ Projected weight: ").append(String.format("%.1f", projectedWeight)).append(" kg\n");
            sb.append("├─ Total gain: ").append(String.format("%.1f", projectedWeight - currentWeight)).append(" kg over ").append(weeks).append(" weeks\n");
            sb.append("├─ Weekly rate: ").append(String.format("%.2f", weeklyWeightChangeKg)).append(" kg/week\n");
        } else {
            sb.append("├─ Weight stays at ").append(String.format("%.1f", currentWeight)).append(" kg\n");
            sb.append("├─ No caloric surplus or deficit—weight maintenance projected\n");
        }

        if (targetProtein > 0 || currentAvgPro > 0) {
            double effectiveProtein = targetProtein > 0 ? targetProtein : currentAvgPro;
            double proteinPerKg = effectiveProtein / currentWeight;
            sb.append("\n").append("Protein assessment:\n");
            sb.append("├─ ").append(String.format("%.1f", effectiveProtein)).append("g protein/day = ")
                    .append(String.format("%.1f", proteinPerKg)).append(" g/kg bodyweight\n");
            if (proteinPerKg >= 2.2) sb.append("├─ Excellent protein for muscle building\n");
            else if (proteinPerKg >= 1.6) sb.append("├─ Good protein for muscle maintenance\n");
            else if (proteinPerKg >= 1.2) sb.append("├─ Adequate but consider increasing to 1.6+ for optimal results\n");
            else sb.append("├─ Low protein intake. Aim for 1.6-2.2g/kg.\n");
        }

        sb.append("\n").append("Recommendation:\n");
        if (isWeightLoss) {
            sb.append("├─ To reach ").append(String.format("%.0f", projectedWeight)).append(" kg in ")
                    .append(weeks).append(" weeks:\n");
            sb.append("│  - Maintain ~").append(String.format("%.0f", Math.abs(weeklyCaloricDeficit / 7.0)))
                    .append(" kcal daily deficit\n");
            sb.append("│  - Priority: high protein (>1.6g/kg) to preserve muscle\n");
            sb.append("│  - Resistance training ").append(trainingFreq).append("x/week to retain strength\n");
        } else {
            sb.append("├─ Build muscle: small surplus (~200-300 kcal) with ").append(trainingFreq).append("x/week training\n");
            sb.append("├─ Progressive overload on compound lifts (bench, squat, deadlift)\n");
            sb.append("├─ Set specific rep/weight goals every 4 weeks\n");
        }
        sb.append("└─ Reassess in ").append(Math.min(weeks, 4)).append(" weeks and adjust based on actual progress\n");

        return sb.toString();
    }

    private int parseInt(String val, int defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
