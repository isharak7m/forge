package com.fitmind.ai.ml;

import com.fitmind.dto.ai.PlateauAlert;
import com.fitmind.entity.ExerciseLog;
import com.fitmind.repository.ExerciseLogRepository;
import com.fitmind.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlateauDetectionService {

    private final ExerciseLogRepository exerciseRepository;
    private final WorkoutSessionRepository workoutRepository;

    public List<PlateauAlert> detectPlateaus(Long userId) {
        List<PlateauAlert> alerts = new ArrayList<>();

        // Strength Plateau Detection
        List<String> exercises = exerciseRepository.findDistinctExerciseNamesByUserId(userId);

        for (String exercise : exercises) {
            List<ExerciseLog> recentLogs = exerciseRepository.findByUserIdAndExerciseNameOrderByDate(userId, exercise);
            if (recentLogs.size() > 5) {
                ExerciseLog last1 = recentLogs.get(recentLogs.size() - 1);
                ExerciseLog last2 = recentLogs.get(recentLogs.size() - 2);
                ExerciseLog last3 = recentLogs.get(recentLogs.size() - 3);

                double vol1 = (last1.getWeightKg() != null ? last1.getWeightKg() : 0.0) * (last1.getReps() != null ? last1.getReps() : 0) * (last1.getSets() != null ? last1.getSets() : 0);
                double vol2 = (last2.getWeightKg() != null ? last2.getWeightKg() : 0.0) * (last2.getReps() != null ? last2.getReps() : 0) * (last2.getSets() != null ? last2.getSets() : 0);
                double vol3 = (last3.getWeightKg() != null ? last3.getWeightKg() : 0.0) * (last3.getReps() != null ? last3.getReps() : 0) * (last3.getSets() != null ? last3.getSets() : 0);

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
}
