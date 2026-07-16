package com.fitmind.ai.ml;

import com.fitmind.dto.ai.WorkoutPrediction;
import com.fitmind.entity.ExerciseLog;
import com.fitmind.entity.User;
import com.fitmind.repository.ExerciseLogRepository;
import com.fitmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutPredictionService {

    private static final double DERIVATIVE_GAINING_THRESHOLD = 0.03;
    private static final double DERIVATIVE_LOSING_THRESHOLD = -0.03;

    private final ExerciseLogRepository exerciseLogRepository;
    private final UserRepository userRepository;

    public WorkoutPrediction predict1RM(Long userId, String exerciseName) {
        User user = userRepository.findById(userId).orElseThrow();
        List<ExerciseLog> logs = exerciseLogRepository.findByUserIdAndExerciseNameOrderByDate(userId, exerciseName);

        if (logs.size() < 3) {
            return generateInsufficientDataPrediction(exerciseName);
        }

        double[] x = new double[logs.size()];
        double[] y = new double[logs.size()];
        LocalDate firstDate = logs.get(0).getWorkoutSession().getDate();

        double currentMax1RM = 0;

        for (int i = 0; i < logs.size(); i++) {
            ExerciseLog log = logs.get(i);
            x[i] = ChronoUnit.DAYS.between(firstDate, log.getWorkoutSession().getDate());
            double w = log.getWeightKg() != null ? log.getWeightKg() : 0.0;
            double r = log.getReps() != null ? log.getReps() : 0.0;
            double estimated1RM = r > 0 ? w * (1.0 + (r / 30.0)) : 0.0;
            y[i] = estimated1RM;

            if (estimated1RM > currentMax1RM) {
                currentMax1RM = estimated1RM;
            }
        }

        long daysSinceFirst = ChronoUnit.DAYS.between(firstDate, LocalDate.now());

        LogisticGrowthModel logisticModel = new LogisticGrowthModel(x, y);
        if (logisticModel.isValid() && logisticModel.getRSquared() > 0.3) {
            return buildLogisticPrediction(exerciseName, logs.size(), logisticModel, currentMax1RM, daysSinceFirst);
        }

        LogRegression model = new LogRegression(x, y);

        double currentPredRaw = model.predict(daysSinceFirst);
        double currentPred = Math.max(currentMax1RM, currentPredRaw);
        double p30 = anchorPrediction(currentMax1RM, currentPredRaw, model.predict(daysSinceFirst + 30), currentPred);
        double p60 = anchorPrediction(currentMax1RM, currentPredRaw, model.predict(daysSinceFirst + 60), currentPred);
        double p90 = anchorPrediction(currentMax1RM, currentPredRaw, model.predict(daysSinceFirst + 90), currentPred);

        double derivative = model.getA() / (daysSinceFirst + 1);
        String trend = derivative > DERIVATIVE_GAINING_THRESHOLD ? "GAINING"
                : (derivative < DERIVATIVE_LOSING_THRESHOLD ? "LOSING" : "STABLE");

        String confidence = model.getRSquared() > 0.7 ? "HIGH"
                : (model.getRSquared() > 0.4 ? "MEDIUM" : "LOW");

        List<String> factors = new ArrayList<>();
        factors.add("Logarithmic regression based on " + logs.size() + " logged sets for " + exerciseName + ".");
        factors.add(String.format("Current daily gain rate: %.2f kg/day (model: %.3f * ln(day+1) + %.2f)",
                derivative, model.getA(), model.getB()));
        factors.add("Logarithmic model captures non-linear strength gains — rapid early progress with gradual plateau over time.");

        return WorkoutPrediction.builder()
                .exerciseName(exerciseName)
                .current1RM(Math.round(currentPred * 10.0) / 10.0)
                .predicted30Days1RM(Math.round(p30 * 10.0) / 10.0)
                .predicted60Days1RM(Math.round(p60 * 10.0) / 10.0)
                .predicted90Days1RM(Math.round(p90 * 10.0) / 10.0)
                .trend(trend)
                .confidence(confidence)
                .methodology("Logarithmic Regression on Epley 1RM Estimates (jnumj logistic fallback failed)")
                .keyFactors(factors)
                .build();
    }

    private WorkoutPrediction buildLogisticPrediction(String exerciseName, int logCount,
                                                       LogisticGrowthModel model, double currentMax1RM,
                                                       long daysSinceFirst) {
        double currentPredRaw = model.predict(daysSinceFirst);
        double currentPred = Math.max(currentMax1RM, currentPredRaw);
        double p30 = anchorPrediction(currentMax1RM, currentPredRaw, model.predict(daysSinceFirst + 30), currentPred);
        double p60 = anchorPrediction(currentMax1RM, currentPredRaw, model.predict(daysSinceFirst + 60), currentPred);
        double p90 = anchorPrediction(currentMax1RM, currentPredRaw, model.predict(daysSinceFirst + 90), currentPred);

        double k = model.getK();
        String trend = k > DERIVATIVE_GAINING_THRESHOLD ? "GAINING"
                : (k < DERIVATIVE_LOSING_THRESHOLD ? "LOSING" : "STABLE");

        String confidence = model.getRSquared() > 0.7 ? "HIGH"
                : (model.getRSquared() > 0.4 ? "MEDIUM" : "LOW");

        List<String> factors = new ArrayList<>();
        factors.add("Logistic growth (S-curve) fit on " + logCount + " logged sets for " + exerciseName + ".");
        factors.add(String.format("Asymptotic max (L): %.1f kg, growth rate (k): %.3f, inflection point (t₀): %.0f days",
                model.getL(), model.getK(), model.getT0()));
        factors.add("S-curve models the natural strength plateau — rapid gains early, tapering toward genetic potential.");
        factors.add(String.format("Model fit (R²): %.2f", model.getRSquared()));

        return WorkoutPrediction.builder()
                .exerciseName(exerciseName)
                .current1RM(Math.round(currentPred * 10.0) / 10.0)
                .predicted30Days1RM(Math.round(p30 * 10.0) / 10.0)
                .predicted60Days1RM(Math.round(p60 * 10.0) / 10.0)
                .predicted90Days1RM(Math.round(p90 * 10.0) / 10.0)
                .trend(trend)
                .confidence(confidence)
                .methodology("Logistic Growth Curve (S-curve) via jnumj Gradient Descent")
                .keyFactors(factors)
                .build();
    }

    public List<String> getAvailableExercises(Long userId) {
        return exerciseLogRepository.findDistinctExerciseNamesByUserId(userId);
    }

    private static double anchorPrediction(double observedMax, double rawCurrent, double rawFuture, double displayCurrent) {
        if (rawCurrent >= observedMax) {
            return Math.max(displayCurrent, rawFuture);
        }
        double delta = rawFuture - rawCurrent;
        return Math.max(displayCurrent, observedMax + delta);
    }

    private WorkoutPrediction generateInsufficientDataPrediction(String exerciseName) {
        List<String> factors = new ArrayList<>();
        factors.add("Insufficient historical exercise data (needs 3+ logged sets).");
        factors.add("Keep logging " + exerciseName + " to unlock AI strength predictions!");

        return WorkoutPrediction.builder()
                .exerciseName(exerciseName)
                .current1RM(0)
                .predicted30Days1RM(0)
                .predicted60Days1RM(0)
                .predicted90Days1RM(0)
                .trend("STABLE")
                .confidence("LOW")
                .methodology("Insufficient Data")
                .keyFactors(factors)
                .build();
    }
}
