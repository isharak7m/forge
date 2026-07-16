package com.fitmind.ai.ml;

import com.fitmind.dto.ai.AdherenceScore;
import com.fitmind.repository.FoodLogRepository;
import com.fitmind.repository.SleepLogRepository;
import com.fitmind.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdherenceService {

    private final WorkoutSessionRepository workoutRepository;
    private final FoodLogRepository foodRepository;
    private final SleepLogRepository sleepLogRepository;

    public AdherenceScore calculateAdherence(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        // Workout (40 pts)
        long workoutCount = workoutRepository.countByUserIdAndDateBetween(userId, thirtyDaysAgo, today);
        double workoutScore = Math.min(40.0, (workoutCount / 12.0) * 40.0);

        // Nutrition (30 pts)
        long foodLogs = foodRepository.countDistinctDatesLogged(userId, thirtyDaysAgo, today);
        double nutritionScore = (foodLogs / 30.0) * 30.0;

        // Sleep (20 pts)
        Double avgSleep = sleepLogRepository.avgSleepHoursByUserIdAndDateBetween(userId, thirtyDaysAgo, today);
        double sleepScore = 10.0;
        if (avgSleep != null) {
            if (avgSleep >= 7.5) sleepScore = 20.0;
            else if (avgSleep >= 6.5) sleepScore = 15.0;
            else if (avgSleep >= 5.5) sleepScore = 10.0;
            else sleepScore = 5.0;
        }

        // Penalties
        double penalty = 0.0;
        if (workoutCount < 4) penalty = 10.0;
        else if (workoutCount < 8) penalty = 5.0;

        double total = Math.max(0, Math.min(100, workoutScore + nutritionScore + sleepScore - penalty));

        String riskLevel = total < 40 ? "HIGH" : (total < 70 ? "MEDIUM" : "LOW");

        List<String> improvements = new ArrayList<>();
        if (workoutScore < 20) improvements.add("Increase weekly workout frequency");
        if (nutritionScore < 15) improvements.add("Log meals more consistently");
        if (sleepScore < 15) improvements.add("Prioritize 7+ hours of sleep");

        return AdherenceScore.builder()
                .overallScore(Math.round(total * 10.0) / 10.0)
                .workoutConsistency(Math.round(workoutScore * 10.0) / 10.0)
                .nutritionConsistency(Math.round(nutritionScore * 10.0) / 10.0)
                .sleepConsistency(Math.round(sleepScore * 10.0) / 10.0)
                .riskLevel(riskLevel)
                .interpretation("Based on last 30 days activity")
                .improvementAreas(improvements)
                .build();
    }
}
