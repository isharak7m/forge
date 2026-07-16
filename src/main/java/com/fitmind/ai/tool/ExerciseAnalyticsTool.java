package com.fitmind.ai.tool;

import com.fitmind.entity.ExerciseLog;
import com.fitmind.repository.ExerciseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ExerciseAnalyticsTool implements AITool {

    private final ExerciseLogRepository exerciseLogRepository;

    @Override
    public String getName() { return "exercise_analytics"; }

    @Override
    public String getDescription() {
        return "Provides a summary of overall exercise performance, trends, exercise diversity, and strongest/weakest areas.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("weeks", "Number of weeks to analyze (default: 4)");
        return p;
    }

    @Override
    public String execute(Long userId, Map<String, String> params) {
        int weeks = parseInt(params.get("weeks"), 4);
        LocalDate from = LocalDate.now().minusWeeks(weeks);
        LocalDate to = LocalDate.now();

        List<ExerciseLog> recentLogs = exerciseLogRepository.findByUserIdAndDateBetween(userId, from, to);
        List<String> allExercises = exerciseLogRepository.findDistinctExerciseNamesByUserId(userId);
        List<ExerciseLog> allLogs = exerciseLogRepository.findAllByUserId(userId);

        if (allLogs.isEmpty()) {
            return "No exercise history found. Log your first workout to get analytics!";
        }

        int totalSessions = (int) allLogs.stream()
                .map(l -> l.getWorkoutSession().getId()).distinct().count();
        int recentSessions = (int) recentLogs.stream()
                .map(l -> l.getWorkoutSession().getId()).distinct().count();

        Map<String, Integer> exerciseFrequency = new LinkedHashMap<>();
        for (ExerciseLog log : allLogs) {
            exerciseFrequency.merge(log.getExerciseName(), 1, Integer::sum);
        }

        List<Map.Entry<String, Integer>> sortedFreq = exerciseFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .toList();

        double totalVolume = 0;
        Map<String, Double> volumeByExercise = new LinkedHashMap<>();
        Map<String, Double> maxWeightByExercise = new LinkedHashMap<>();

        for (ExerciseLog log : allLogs) {
            double w = log.getWeightKg() != null ? log.getWeightKg() : 0;
            double r = log.getReps() != null ? log.getReps() : 0;
            double s = log.getSets() != null ? log.getSets() : 1;
            double vol = w * r * s;
            totalVolume += vol;
            volumeByExercise.merge(log.getExerciseName(), vol, Double::sum);
            maxWeightByExercise.merge(log.getExerciseName(), w, Math::max);
        }

        String[] timeFrames = {"7 days", "30 days", "90 days", "All time"};
        int[] dayCounts = {7, 30, 90, 36500};
        StringBuilder timeSeries = new StringBuilder();
        for (int i = 0; i < timeFrames.length; i++) {
            int count = dayCounts[i] >= 36500 ? allLogs.size() :
                    exerciseLogRepository.findByUserIdAndDateBetween(userId,
                            LocalDate.now().minusDays(dayCounts[i]), LocalDate.now()).size();
            timeSeries.append("  ").append(timeFrames[i]).append(": ").append(count).append(" sets\n");
        }

        double avgWeight = allLogs.stream().filter(l -> l.getWeightKg() != null)
                .mapToDouble(ExerciseLog::getWeightKg).average().orElse(0);
        double avgReps = allLogs.stream().filter(l -> l.getReps() != null)
                .mapToDouble(ExerciseLog::getReps).average().orElse(0);
        double avgSets = allLogs.stream().filter(l -> l.getSets() != null)
                .mapToDouble(ExerciseLog::getSets).average().orElse(1);
        double avgVolume = allLogs.stream()
                .mapToDouble(l -> (l.getWeightKg() != null ? l.getWeightKg() : 0) *
                        (l.getReps() != null ? l.getReps() : 0) * (l.getSets() != null ? l.getSets() : 1))
                .average().orElse(0);

        StringBuilder sb = new StringBuilder();
        sb.append("Exercise Analytics (last ").append(weeks).append(" weeks):\n");

        sb.append("\n").append("By the numbers:\n");
        sb.append("├─ Total workout sessions: ").append(totalSessions).append("\n");
        sb.append("├─ Recent sessions (last ").append(weeks).append("w): ").append(recentSessions).append("\n");
        sb.append("├─ Unique exercises: ").append(allExercises.size()).append("\n");
        sb.append("├─ Avg per set across all history: ").append(String.format("%.1f", avgWeight)).append("kg × ")
                .append(String.format("%.0f", avgReps)).append(" reps × ")
                .append(String.format("%.0f", avgSets)).append(" sets = ")
                .append(String.format("%.0f", avgVolume)).append(" kg volume\n");
        sb.append("├─ Total lifetime volume: ").append(String.format("%.0f", totalVolume)).append(" kg\n");

        sb.append("\n").append("Training volume over time:\n").append(timeSeries);

        sb.append("\n").append("Most frequent exercises:\n");
        int rank = 1;
        for (var e : sortedFreq.stream().limit(8).toList()) {
            sb.append(rank++).append(". ").append(e.getKey()).append(": ").append(e.getValue()).append(" sets\n");
        }

        sb.append("\n").append("Heaviest lifts:\n");
        maxWeightByExercise.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append("├─ ").append(e.getKey()).append(": ")
                        .append(String.format("%.1f", e.getValue())).append("kg\n"));

        if (sortedFreq.size() >= 3) {
            String mostTrained = sortedFreq.get(0).getKey();
            String leastTrained = sortedFreq.get(sortedFreq.size() - 1).getKey();
            sb.append("\n").append("Balance note: You have trained ").append(mostTrained)
                    .append(" ").append(sortedFreq.get(0).getValue()).append(" times vs ")
                    .append(leastTrained).append(" only ").append(sortedFreq.get(sortedFreq.size() - 1).getValue())
                    .append(" time(s). Consider balancing your program.");
        }

        if (recentSessions == 0) {
            sb.append("\n\n").append("No workouts logged in the last ").append(weeks).append(" weeks. Time to get back in the gym!");
        } else if (recentSessions < 3) {
            sb.append("\n\n").append("Only ").append(recentSessions).append(" sessions in ").append(weeks)
                    .append(" weeks. Aim for 3-6 sessions/week for optimal progress.");
        }

        return sb.toString();
    }

    private int parseInt(String val, int defaultVal) {
        if (val == null || val.isEmpty()) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
