package com.fitmind.ai.assistant;

import com.fitmind.ai.ml.PredictionService;
import com.fitmind.dto.ai.AssistantResponse;
import com.fitmind.entity.ExerciseLog;
import com.fitmind.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FitnessAssistantService {

    private final UserRepository userRepository;
    private final FoodLogRepository foodRepository;
    private final WorkoutSessionRepository workoutRepository;
    private final ExerciseLogRepository exerciseRepository;
    private final BodyMetricRepository metricRepository;
    private final PredictionService predictionService;

    public AssistantResponse processQuery(Long userId, String query) {
        String lowerQuery = query.toLowerCase();
        QueryIntent intent = detectIntent(lowerQuery);
        
        List<String> sources = new ArrayList<>();
        String response;

        // Simplify dates to last 30 days for this demo
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30);

        switch (intent) {
            case CALORIE_QUERY:
                Double cals = foodRepository.sumCaloriesByUserIdAndDateBetween(userId, from, to);
                if (cals == null || cals == 0) {
                    response = "I couldn't find any calorie data for the last 30 days. Start logging meals to see your averages!";
                } else {
                    response = String.format("Over the last 30 days, you have logged a total of %.0f calories, averaging %.0f calories per day.", cals, cals/30.0);
                    sources.add("Nutrition Logs (Last 30 Days)");
                }
                break;
                
            case WEIGHT_QUERY:
                Double avgWeight = metricRepository.avgWeightByUserIdAndDateBetween(userId, from, to);
                if (avgWeight == null) {
                    response = "You haven't logged any weight data recently.";
                } else {
                    response = String.format("Your average weight over the last 30 days is %.1f kg.", avgWeight);
                    sources.add("Body Metrics (Last 30 Days)");
                }
                break;
                
            case PR_QUERY:
                // very simple PR extraction
                List<String> exercises = exerciseRepository.findDistinctExerciseNamesByUserId(userId);
                if (exercises.isEmpty()) {
                    response = "You haven't logged any exercises yet.";
                } else {
                    String ex = exercises.get(0);
                    List<ExerciseLog> prs = exerciseRepository.findPersonalRecordsByExercise(userId, ex);
                    if (!prs.isEmpty()) {
                        ExerciseLog pr = prs.get(0);
                        double epley = pr.getWeightKg() * (1 + (pr.getReps() / 30.0));
                        response = String.format("Your best lift for %s was %.1f kg for %d reps. Estimated 1RM: %.1f kg.", 
                                ex, pr.getWeightKg(), pr.getReps(), epley);
                        sources.add("Exercise Logs");
                    } else {
                        response = "Couldn't calculate PRs.";
                    }
                }
                break;
                
            case PREDICTION_QUERY:
                var pred = predictionService.predictWeight(userId);
                response = String.format("Based on your data, your weight is trending %s. Your predicted weight in 30 days is %.1f kg.", 
                        pred.getTrend(), pred.getPredicted30Days());
                sources.add("Prediction Engine");
                break;

            default:
                response = "I am a specialized fitness AI. Ask me about your calories, weight, personal records, or future predictions!";
        }

        return AssistantResponse.builder()
                .query(query)
                .response(response)
                .intent(intent.name())
                .dataSources(sources)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private QueryIntent detectIntent(String q) {
        if (q.contains("calori") || q.contains("kcal") || q.contains("eat")) return QueryIntent.CALORIE_QUERY;
        if (q.contains("weight") || q.contains("weigh") || q.contains("kg")) return QueryIntent.WEIGHT_QUERY;
        if (q.contains("pr") || q.contains("best") || q.contains("record")) return QueryIntent.PR_QUERY;
        if (q.contains("predict") || q.contains("future")) return QueryIntent.PREDICTION_QUERY;
        return QueryIntent.GENERAL_QUERY;
    }
}
