package com.forge.ai.ml;

import com.forge.dto.ai.WeightPrediction;
import com.forge.entity.User;
import com.forge.entity.weight.WeightLog;
import com.forge.repository.UserRepository;
import com.forge.repository.WeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final UserRepository userRepository;
    private final WeightLogRepository weightLogRepository;

    public WeightPrediction predictWeight(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<WeightLog> logs = weightLogRepository.findByUserIdOrderByDateAsc(userId);

        if (logs.size() < 3) {
            return generateTdeeEstimate(user);
        }

        return trainAndPredict(user, logs);
    }

    private WeightPrediction trainAndPredict(User user, List<WeightLog> logs) {
        int n = logs.size();
        double[] x = new double[n];
        double[] y = new double[n];

        LocalDate startDate = logs.get(0).getDate();
        for (int i = 0; i < n; i++) {
            x[i] = ChronoUnit.DAYS.between(startDate, logs.get(i).getDate());
            y[i] = logs.get(i).getWeightKg();
        }

        LinearRegressionModel model = new LinearRegressionModel(x, y);
        double currentWeight = logs.get(n - 1).getWeightKg();

        double pred30 = model.predict(ChronoUnit.DAYS.between(startDate, LocalDate.now().plusDays(30)));
        double pred60 = model.predict(ChronoUnit.DAYS.between(startDate, LocalDate.now().plusDays(60)));
        double pred90 = model.predict(ChronoUnit.DAYS.between(startDate, LocalDate.now().plusDays(90)));

        double slope = model.getSlope();
        String trend = Math.abs(slope) < 0.01 ? "STABLE" : (slope < 0 ? "LOSING" : "GAINING");

        String confidence;
        if (n >= 20 && model.getRSquared() > 0.7) {
            confidence = "HIGH";
        } else if (n >= 10) {
            confidence = "MEDIUM";
        } else {
            confidence = "LOW";
        }

        List<String> factors = new ArrayList<>();
        factors.add("Based on " + n + " weight log entries.");
        factors.add("Model fit (R²): " + String.format("%.2f", model.getRSquared()));
        factors.add(String.format("Linear trend: %.3f kg/day", slope));
        if (trend.equals("LOSING")) {
            factors.add("You are trending downward. Maintain your current habits.");
        } else if (trend.equals("GAINING")) {
            factors.add("Weight is trending upward. Review nutrition if this is unintended.");
        }

        return WeightPrediction.builder()
                .currentWeight(currentWeight)
                .predicted30Days(Math.round(pred30 * 10.0) / 10.0)
                .predicted60Days(Math.round(pred60 * 10.0) / 10.0)
                .predicted90Days(Math.round(pred90 * 10.0) / 10.0)
                .trend(trend)
                .confidence(confidence)
                .methodology("Linear Regression (OLS via jnumj matmul/solve)")
                .keyFactors(factors)
                .build();
    }

    private WeightPrediction generateTdeeEstimate(User user) {
        double weight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 70.0;
        List<String> factors = new ArrayList<>();
        factors.add("Insufficient weight history for trend analysis.");
        factors.add("Log weight entries daily for personalized predictions.");

        return WeightPrediction.builder()
                .currentWeight(weight)
                .predicted30Days(weight)
                .predicted60Days(weight)
                .predicted90Days(weight)
                .trend("STABLE")
                .confidence("LOW")
                .methodology("Current Weight Estimate (Insufficient Historical Data)")
                .keyFactors(factors)
                .build();
    }
}
