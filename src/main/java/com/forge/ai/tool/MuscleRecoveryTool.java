package com.forge.ai.tool;

import com.forge.entity.ExerciseLog;
import com.forge.repository.ExerciseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MuscleRecoveryTool implements AITool {

    private static final Map<String, List<String>> EXERCISE_MUSCLE_MAP = new LinkedHashMap<>();
    static {
        EXERCISE_MUSCLE_MAP.put("chest", List.of("bench press", "chest press", "incline bench", "dumbbell fly", "cable fly", "push up"));
        EXERCISE_MUSCLE_MAP.put("back", List.of("pull up", "lat pulldown", "barbell row", "dumbbell row", "deadlift", "face pull", "seated row"));
        EXERCISE_MUSCLE_MAP.put("shoulders", List.of("overhead press", "shoulder press", "lateral raise", "front raise", "reverse fly", "arnold press"));
        EXERCISE_MUSCLE_MAP.put("biceps", List.of("bicep curl", "dumbbell curl", "barbell curl", "hammer curl", "preacher curl"));
        EXERCISE_MUSCLE_MAP.put("triceps", List.of("tricep pushdown", "tricep extension", "skull crusher", "close grip bench", "dip"));
        EXERCISE_MUSCLE_MAP.put("legs", List.of("squat", "leg press", "leg extension", "leg curl", "bulgarian split squat", "goblet squat", "lunges"));
        EXERCISE_MUSCLE_MAP.put("glutes", List.of("hip thrust", "glute bridge", "cable kickback", "deadlift", "squat"));
        EXERCISE_MUSCLE_MAP.put("core", List.of("plank", "crunch", "leg raise", "russian twist", "cable crunch", "ab wheel"));
    }

    private final ExerciseLogRepository exerciseLogRepository;

    @Override
    public String getName() { return "muscle_recovery"; }

    @Override
    public String getDescription() {
        return "Analyzes recovery status for different muscle groups based on recent training frequency and volume.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("muscleGroup", "Specific muscle group (chest, back, shoulders, legs, arms, core). Leave empty for full analysis.");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        String muscleFilter = params.getOrDefault("muscleGroup", "").toLowerCase().trim();

        LocalDate now = LocalDate.now();
        List<ExerciseLog> allLogs = exerciseLogRepository.findAllByUserId(userId);
        if (allLogs.isEmpty()) {
            return "No exercise history available. Log workouts to get recovery analysis.";
        }

        Map<String, List<ExerciseLog>> logsByMuscle = new LinkedHashMap<>();
        for (var entry : EXERCISE_MUSCLE_MAP.entrySet()) {
            String muscle = entry.getKey();
            List<String> exercises = entry.getValue();
            List<ExerciseLog> muscleLogs = allLogs.stream()
                    .filter(l -> exercises.stream().anyMatch(ex -> l.getExerciseName().toLowerCase().contains(ex)))
                    .toList();
            logsByMuscle.put(muscle, new ArrayList<>(muscleLogs));
        }

        StringBuilder sb = new StringBuilder();
        if (muscleFilter.isEmpty()) {
            sb.append("Muscle Recovery Status:\n\n");
        } else {
            sb.append("Recovery analysis for ").append(muscleFilter).append(":\n\n");
        }

        for (var entry : logsByMuscle.entrySet()) {
            String muscle = entry.getKey();
            List<ExerciseLog> logs = entry.getValue();

            if (!muscleFilter.isEmpty() && !muscle.equals(muscleFilter) &&
                    !muscleFilter.contains(muscle)) continue;

            if (muscleFilter.isEmpty()) {
                sb.append(muscle).append(":\n");
            }

            if (logs.isEmpty()) {
                sb.append("├─ Status: READY (no recent training data)\n\n");
                continue;
            }

            ExerciseLog lastLog = logs.get(logs.size() - 1);
            LocalDate lastDate = lastLog.getWorkoutSession().getDate();
            long daysSince = ChronoUnit.DAYS.between(lastDate, now);

            long sevenDayCount = logs.stream()
                    .filter(l -> ChronoUnit.DAYS.between(l.getWorkoutSession().getDate(), now) <= 7)
                    .count();

            long fourteenDayCount = logs.stream()
                    .filter(l -> ChronoUnit.DAYS.between(l.getWorkoutSession().getDate(), now) <= 14)
                    .count();

            String recoveryStatus;
            int recommendedRestDays;

            if (daysSince <= 1) {
                recoveryStatus = "NOT RECOVERED — just trained, rest needed";
                recommendedRestDays = 2;
            } else if (daysSince <= 2) {
                recoveryStatus = "RECOVERING — 48h window";
                recommendedRestDays = 1;
            } else if (daysSince <= 3) {
                if (sevenDayCount <= 1) {
                    recoveryStatus = "MOSTLY RECOVERED — can retrain";
                    recommendedRestDays = 0;
                } else {
                    recoveryStatus = "ADEQUATE REST — trained " + sevenDayCount + "x this week";
                    recommendedRestDays = 0;
                }
            } else {
                recoveryStatus = "READY — fully recovered";
                recommendedRestDays = 0;
            }

            double avgVolumePerSession = 0;
            if (!logs.isEmpty()) {
                avgVolumePerSession = logs.stream()
                        .mapToDouble(l -> (l.getWeightKg() != null ? l.getWeightKg() : 0) *
                                (l.getReps() != null ? l.getReps() : 0) * (l.getSets() != null ? l.getSets() : 1))
                        .average().orElse(0);
            }

            sb.append("├─ Last trained: ").append(daysSince).append(" days ago\n");
            sb.append("├─ Status: ").append(recoveryStatus).append("\n");
            sb.append("├─ Sessions last 7 days: ").append(sevenDayCount).append("\n");
            sb.append("├─ Sessions last 14 days: ").append(fourteenDayCount).append("\n");
            sb.append("├─ Avg volume per session: ").append(String.format("%.0f", avgVolumePerSession)).append(" kg\n");

            if (sevenDayCount >= 2 && daysSince <= 1) {
                sb.append("├─ ⚠ High training frequency — watch for cumulative fatigue\n");
            }
            if (sevenDayCount == 0 && daysSince > 7) {
                sb.append("├─ Not trained recently — ease back in, don't go 100%\n");
            }

            if (muscleFilter.isEmpty()) sb.append("\n");
        }

        if (!muscleFilter.isEmpty()) {
            boolean found = EXERCISE_MUSCLE_MAP.containsKey(muscleFilter) ||
                    EXERCISE_MUSCLE_MAP.keySet().stream().anyMatch(k -> muscleFilter.contains(k));
            if (!found) {
                sb.append("Unknown muscle group. Options: ")
                        .append(String.join(", ", EXERCISE_MUSCLE_MAP.keySet())).append("\n");
            }
        }

        return sb.toString();
    }
}
