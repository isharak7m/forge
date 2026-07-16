package com.fitmind.ai.tool;

import com.fitmind.entity.ExerciseLog;
import com.fitmind.repository.ExerciseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ProgressiveOverloadTool implements AITool {

    private final ExerciseLogRepository exerciseLogRepository;

    @Override
    public String getName() { return "progressive_overload"; }

    @Override
    public String getDescription() {
        return "Analyze exercise history and recommend next weight/reps using progressive overload principles including estimated 1RM.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("exercise", "Exercise name (e.g., bench press, squat, deadlift)");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        String exerciseName = params.getOrDefault("exercise", "");
        if (exerciseName.isEmpty()) {
            List<String> allExercises = exerciseLogRepository.findDistinctExerciseNamesByUserId(userId);
            if (allExercises.isEmpty()) {
                return "No exercise history found. Log some workouts first!";
            }
            exerciseName = allExercises.get(0);
        }

        List<ExerciseLog> logs = exerciseLogRepository.findByUserIdAndExerciseNameOrderByDate(userId, exerciseName);
        if (logs.isEmpty()) {
            return "No workout history found for '" + exerciseName + "'.";
        }

        int totalSets = logs.size();
        LocalDate firstDate = logs.get(0).getWorkoutSession().getDate();
        LocalDate lastDate = logs.get(logs.size() - 1).getWorkoutSession().getDate();
        long daysTrained = ChronoUnit.DAYS.between(firstDate, lastDate);
        if (daysTrained < 1) daysTrained = 1;

        double bestSetVolume = 0;
        double bestWeight = 0;
        int bestReps = 0;
        double latestVolume = 0;
        double secondLatestVolume = 0;
        double estimated1RM = 0;

        if (logs.size() >= 2) {
            ExerciseLog latest = logs.get(logs.size() - 1);
            ExerciseLog prev = logs.get(logs.size() - 2);
            double w1 = latest.getWeightKg() != null ? latest.getWeightKg() : 0;
            double r1 = latest.getReps() != null ? latest.getReps() : 0;
            double w2 = prev.getWeightKg() != null ? prev.getWeightKg() : 0;
            double r2 = prev.getReps() != null ? prev.getReps() : 0;
            latestVolume = w1 * r1 * (latest.getSets() != null ? latest.getSets() : 1);
            secondLatestVolume = w2 * r2 * (prev.getSets() != null ? prev.getSets() : 1);
        }

        for (ExerciseLog log : logs) {
            double w = log.getWeightKg() != null ? log.getWeightKg() : 0;
            double r = log.getReps() != null ? log.getReps() : 0;
            double s = log.getSets() != null ? log.getSets() : 1;
            double volume = w * r * s;
            if (volume > bestSetVolume) {
                bestSetVolume = volume;
                bestWeight = w;
                bestReps = (int) r;
            }
            double e1RM = w * (1.0 + (r / 30.0));
            if (e1RM > estimated1RM) estimated1RM = e1RM;
        }

        double avgWeight = logs.stream().filter(l -> l.getWeightKg() != null).mapToDouble(ExerciseLog::getWeightKg).average().orElse(0);
        double avgReps = logs.stream().filter(l -> l.getReps() != null).mapToDouble(ExerciseLog::getReps).average().orElse(0);
        double avgSets = logs.stream().filter(l -> l.getSets() != null).mapToDouble(ExerciseLog::getSets).average().orElse(1);
        int totalVolume = (int) logs.stream().mapToDouble(l -> (l.getWeightKg() != null ? l.getWeightKg() : 0) *
                (l.getReps() != null ? l.getReps() : 0) * (l.getSets() != null ? l.getSets() : 1)).sum();

        double volumeChange = 0;
        if (secondLatestVolume > 0) {
            volumeChange = ((latestVolume - secondLatestVolume) / secondLatestVolume) * 100;
        }

        double recommendedWeight;
        String overloadStrategy;

        if (bestReps >= 12 && bestWeight > 0) {
            recommendedWeight = bestWeight + 2.5;
            overloadStrategy = "Increase weight by 2.5kg (you hit " + bestReps + " reps, ready for heavier load)";
        } else if (bestReps >= 8 && bestWeight > 0) {
            recommendedWeight = bestWeight + 1.25;
            overloadStrategy = "Add 1.25kg micro-load (you are in 8-12 rep range, small increments work best)";
        } else if (bestReps >= 5 && bestWeight > 0) {
            recommendedWeight = bestWeight + 2.5;
            overloadStrategy = "Increase by 2.5kg (strength focus: 5+ reps with heavier weight)";
        } else {
            recommendedWeight = bestWeight;
            overloadStrategy = "Focus on adding reps first before increasing weight (aim for 8+ reps)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Progressive Overload Analysis: ").append(exerciseName).append("\n");
        sb.append("├─ Total logged sets: ").append(totalSets).append(" over ").append(daysTrained).append(" days\n");
        sb.append("├─ Average: ").append(String.format("%.1f", avgWeight)).append("kg × ")
                .append(String.format("%.0f", avgReps)).append(" reps × ")
                .append(String.format("%.0f", avgSets)).append(" sets\n");
        sb.append("├─ Best recorded set: ").append(String.format("%.1f", bestWeight)).append("kg × ").append(bestReps).append(" reps\n");
        sb.append("├─ Estimated 1RM: ").append(String.format("%.1f", estimated1RM)).append("kg (Epley formula)\n");
        sb.append("├─ Total volume over period: ").append(totalVolume).append(" kg\n");
        if (logs.size() >= 2) {
            sb.append("├─ Volume trend: ").append(String.format("%+.1f", volumeChange)).append("% (last session vs previous)\n");
        }

        sb.append("\n").append("Recommendation:\n");
        sb.append("├─ Next working weight: ").append(String.format("%.1f", recommendedWeight)).append("kg\n");
        sb.append("├─ Strategy: ").append(overloadStrategy).append("\n");
        sb.append("├─ Target reps: 8-12 for hypertrophy, 3-6 for strength\n");
        sb.append("└─ Estimated 1RM: ").append(String.format("%.1f", estimated1RM)).append("kg — use 75-85% for working sets\n");

        int totalDays = Math.max((int) daysTrained, 1);
        double weeklyVolume = totalVolume / (totalDays / 7.0);
        sb.append("\n").append("Volume guidance: ").append(String.format("%.0f", weeklyVolume)).append(" kg/week total. ");
        if (volumeChange > 10) {
            sb.append("Your volume jumped significantly last session—be careful not to overreach.");
        } else if (volumeChange < -10) {
            sb.append("Volume dropped. Consider ramping back up to previous intensity.");
        } else {
            sb.append("Volume is stable. Consistent, progressive overload builds strength.");
        }

        return sb.toString();
    }
}
