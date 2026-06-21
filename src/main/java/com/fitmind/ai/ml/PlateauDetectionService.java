package com.fitmind.ai.ml;

import com.fitmind.dto.ai.PlateauAlert;
import com.fitmind.entity.BodyMetric;
import com.fitmind.entity.ExerciseLog;
import com.fitmind.repository.BodyMetricRepository;
import com.fitmind.repository.ExerciseLogRepository;
import com.fitmind.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlateauDetectionService {

    private final BodyMetricRepository metricRepository;
    private final ExerciseLogRepository exerciseRepository;
    private final WorkoutSessionRepository workoutRepository;

    public List<PlateauAlert> detectPlateaus(Long userId) {
        List<PlateauAlert> alerts = new ArrayList<>();
        
        // 1. Weight Plateau
        LocalDate threeWeeksAgo = LocalDate.now().minusDays(21);
        List<BodyMetric> recentMetrics = metricRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                userId, threeWeeksAgo, LocalDate.now());
                
        List<Double> weights = recentMetrics.stream()
                .filter(m -> m.getWeightKg() != null)
                .map(BodyMetric::getWeightKg)
                .collect(Collectors.toList());

        if (weights.size() >= 5) {
            double stdDev = standardDeviation(weights);
            if (stdDev < 0.5) {
                String severity = stdDev < 0.2 ? "HIGH" : (stdDev < 0.35 ? "MEDIUM" : "LOW");
                alerts.add(PlateauAlert.builder()
                        .type("WEIGHT")
                        .description("Weight has not significantly changed in the last 21 days.")
                        .daysStagnant(21)
                        .affectedMetric("Body Weight")
                        .severity(severity)
                        .detectedSince(recentMetrics.get(0).getRecordedDate())
                        .build());
            }
        }

        // 2. Strength Plateau (Simplified for brevity)
        List<String> exercises = exerciseRepository.findDistinctExerciseNamesByUserId(userId);
        LocalDate fourWeeksAgo = LocalDate.now().minusDays(28);
        LocalDate twoWeeksAgo = LocalDate.now().minusDays(14);
        
        for (String exercise : exercises) {
            List<ExerciseLog> recentLogs = exerciseRepository.findByUserIdAndExerciseNameOrderByDate(userId, exercise);
            // In a real implementation, we'd filter by date and compare weeks 1-2 vs weeks 3-4
            // Mock logic for demonstration:
            if (recentLogs.size() > 5) {
                // If last 3 sessions have exactly same weight/reps/sets
                ExerciseLog last1 = recentLogs.get(recentLogs.size() - 1);
                ExerciseLog last2 = recentLogs.get(recentLogs.size() - 2);
                ExerciseLog last3 = recentLogs.get(recentLogs.size() - 3);
                
                double vol1 = last1.getWeightKg() * last1.getReps() * last1.getSets();
                double vol2 = last2.getWeightKg() * last2.getReps() * last2.getSets();
                double vol3 = last3.getWeightKg() * last3.getReps() * last3.getSets();
                
                if (Math.abs(vol1 - vol2) < 1.0 && Math.abs(vol2 - vol3) < 1.0) {
                    alerts.add(PlateauAlert.builder()
                            .type("STRENGTH")
                            .description(exercise + " volume has stagnated across recent sessions.")
                            .daysStagnant(14)
                            .affectedMetric(exercise)
                            .severity("MEDIUM")
                            .detectedSince(last3.getWorkoutSession().getDate())
                            .build());
                }
            }
        }

        return alerts;
    }

    private double standardDeviation(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double mean = values.stream().mapToDouble(v -> v).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance);
    }
}
