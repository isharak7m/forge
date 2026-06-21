package com.fitmind.ai.ml;

import com.fitmind.dto.ai.WeightPrediction;
import com.fitmind.entity.BodyMetric;
import com.fitmind.entity.User;
import com.fitmind.entity.enums.Gender;
import com.fitmind.repository.BodyMetricRepository;
import com.fitmind.repository.FoodLogRepository;
import com.fitmind.repository.UserRepository;
import com.fitmind.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final BodyMetricRepository metricRepository;
    private final FoodLogRepository foodRepository;
    private final WorkoutSessionRepository workoutRepository;
    private final UserRepository userRepository;

    public WeightPrediction predictWeight(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<BodyMetric> metrics = metricRepository.findByUserIdOrderByRecordedDateDesc(userId);
        
        if (metrics.size() < 5) {
            return generateTdeeEstimate(user);
        }

        // Reverse to chronological order
        List<BodyMetric> chronoMetrics = new ArrayList<>();
        for (int i = metrics.size() - 1; i >= 0; i--) {
            if (metrics.get(i).getWeightKg() != null) {
                chronoMetrics.add(metrics.get(i));
            }
        }

        if (chronoMetrics.size() < 5) {
             return generateTdeeEstimate(user);
        }

        double[] x = new double[chronoMetrics.size()];
        double[] y = new double[chronoMetrics.size()];
        LocalDate firstDate = chronoMetrics.get(0).getRecordedDate();
        LocalDate lastDate = chronoMetrics.get(chronoMetrics.size() - 1).getRecordedDate();
        
        for (int i = 0; i < chronoMetrics.size(); i++) {
            x[i] = ChronoUnit.DAYS.between(firstDate, chronoMetrics.get(i).getRecordedDate());
            y[i] = chronoMetrics.get(i).getWeightKg();
        }

        SimpleLinearRegression model = new SimpleLinearRegression(x, y);
        
        long daysSinceFirst = ChronoUnit.DAYS.between(firstDate, LocalDate.now());
        double currentPred = model.predict(daysSinceFirst);
        double p30 = model.predict(daysSinceFirst + 30);
        double p60 = model.predict(daysSinceFirst + 60);
        double p90 = model.predict(daysSinceFirst + 90);

        String trend = model.getSlope() > 0.05 ? "GAINING" : (model.getSlope() < -0.05 ? "LOSING" : "STABLE");
        String confidence = model.getRSquared() > 0.7 ? "HIGH" : (model.getRSquared() > 0.4 ? "MEDIUM" : "LOW");
        
        List<String> factors = new ArrayList<>();
        factors.add("Linear regression based on " + chronoMetrics.size() + " weight entries.");
        factors.add(String.format("Current rate: %.2f kg/month", model.getSlope() * 30));

        return WeightPrediction.builder()
                .currentWeight(user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : currentPred)
                .predicted30Days(Math.round(p30 * 10.0) / 10.0)
                .predicted60Days(Math.round(p60 * 10.0) / 10.0)
                .predicted90Days(Math.round(p90 * 10.0) / 10.0)
                .trend(trend)
                .confidence(confidence)
                .methodology("Ordinary Least Squares (OLS) Linear Regression")
                .keyFactors(factors)
                .build();
    }

    private WeightPrediction generateTdeeEstimate(User user) {
        double weight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 70.0;
        List<String> factors = new ArrayList<>();
        factors.add("Insufficient historical weight data (needs 5+ entries).");
        factors.add("Using theoretical TDEE estimation.");
        
        return WeightPrediction.builder()
                .currentWeight(weight)
                .predicted30Days(weight)
                .predicted60Days(weight)
                .predicted90Days(weight)
                .trend("STABLE")
                .confidence("LOW")
                .methodology("TDEE Base Estimate (Insufficient Data)")
                .keyFactors(factors)
                .build();
    }
}
