package com.fitmind.ai.recommendation;

import com.fitmind.ai.ml.AdherenceService;
import com.fitmind.ai.ml.PlateauDetectionService;
import com.fitmind.ai.ml.PredictionService;
import com.fitmind.dto.ai.PlateauAlert;
import com.fitmind.dto.ai.Recommendation;
import com.fitmind.entity.User;
import com.fitmind.entity.enums.FitnessGoal;
import com.fitmind.entity.enums.Gender;
import com.fitmind.repository.BodyMetricRepository;
import com.fitmind.repository.FoodLogRepository;
import com.fitmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PredictionService predictionService;
    private final PlateauDetectionService plateauService;
    private final AdherenceService adherenceService;
    private final BodyMetricRepository metricRepository;
    private final FoodLogRepository foodRepository;
    private final UserRepository userRepository;

    public List<Recommendation> generateRecommendations(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Recommendation> recs = new ArrayList<>();
        
        var plateaus = plateauService.detectPlateaus(userId);
        var adherence = adherenceService.calculateAdherence(userId);
        
        LocalDate twoWeeksAgo = LocalDate.now().minusDays(14);
        Double avgCalories = foodRepository.sumCaloriesByUserIdAndDateBetween(userId, twoWeeksAgo, LocalDate.now());
        if (avgCalories != null) avgCalories /= 14.0;
        else avgCalories = 0.0;

        double tdee = calculateTDEE(user);

        // Rule 1 & 2: Weight Plateaus
        boolean hasWeightPlateau = plateaus.stream().anyMatch(p -> p.getType().equals("WEIGHT"));
        if (hasWeightPlateau && avgCalories > 0) {
            if (avgCalories > tdee * 0.95 && user.getFitnessGoal() == FitnessGoal.MUSCLE_GAIN) {
                recs.add(Recommendation.builder()
                        .category("NUTRITION")
                        .title("Increase Caloric Intake")
                        .description("Increase calories by 200-300 kcal/day.")
                        .reason("Weight has plateaued despite being near your theoretical maintenance. Additional calories are needed to drive hypertrophy.")
                        .priority("HIGH")
                        .actionItem("Update nutrition targets")
                        .build());
            } else if (avgCalories < tdee * 0.85 && user.getFitnessGoal() == FitnessGoal.FAT_LOSS) {
                recs.add(Recommendation.builder()
                        .category("NUTRITION")
                        .title("Implement a Diet Break")
                        .description("Return to maintenance calories for 1-2 weeks.")
                        .reason("Metabolic adaptation has likely occurred after an extended deficit, causing a weight plateau.")
                        .priority("HIGH")
                        .actionItem("Set calories to maintenance")
                        .build());
            }
        }

        // Rule 3: Strength Plateaus
        long strengthPlateaus = plateaus.stream().filter(p -> p.getType().equals("STRENGTH")).count();
        if (strengthPlateaus > 0) {
            recs.add(Recommendation.builder()
                    .category("TRAINING")
                    .title("Schedule a Deload Week")
                    .description("Reduce training volume by 40-50% for one week.")
                    .reason(strengthPlateaus + " exercises are showing stagnation. A deload will dissipate accumulated fatigue.")
                    .priority("HIGH")
                    .actionItem("Plan deload workouts")
                    .build());
        }

        // Rule 4: Poor Sleep
        Double avgSleep = metricRepository.avgSleepHoursByUserIdAndDateBetween(userId, twoWeeksAgo, LocalDate.now());
        if (avgSleep != null && avgSleep < 7.0) {
            recs.add(Recommendation.builder()
                    .category("RECOVERY")
                    .title("Prioritize Sleep Duration")
                    .description("Aim for at least 7.5 hours of sleep per night.")
                    .reason("Your 14-day average sleep is " + String.format("%.1f", avgSleep) + "h. Suboptimal sleep severely impairs recovery and muscle protein synthesis.")
                    .priority("MEDIUM")
                    .actionItem("Set a consistent bedtime")
                    .build());
        }

        // Rule 5: Low Adherence
        if (adherence.getOverallScore() < 50) {
            recs.add(Recommendation.builder()
                    .category("ADHERENCE")
                    .title("Simplify Your Routine")
                    .description("Focus on 2-3 manageable habits rather than the full program.")
                    .reason("Current adherence is " + adherence.getOverallScore() + "%. The current schedule may be unsustainable.")
                    .priority("HIGH")
                    .actionItem("Review current schedule")
                    .build());
        }
        
        // Rule 6: Protein Need
        Double totalProtein = foodRepository.sumProteinByUserIdAndDateBetween(userId, twoWeeksAgo, LocalDate.now());
        double weight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 70.0;
        if (totalProtein != null && (totalProtein / 14.0) < (weight * 1.6) && user.getFitnessGoal() == FitnessGoal.MUSCLE_GAIN) {
            recs.add(Recommendation.builder()
                    .category("NUTRITION")
                    .title("Increase Protein Intake")
                    .description("Aim for " + Math.round(weight * 1.8) + "g of protein daily.")
                    .reason("Current protein is sub-optimal for muscle gain.")
                    .priority("MEDIUM")
                    .actionItem("Adjust meal macros")
                    .build());
        }

        return recs;
    }

    private double calculateTDEE(User user) {
        double weight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg() : 70.0;
        double height = user.getHeightCm() != null ? user.getHeightCm() : 170.0;
        int age = user.getAge() != null ? user.getAge() : 30;
        
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        if (user.getGender() == Gender.MALE) bmr += 5;
        else if (user.getGender() == Gender.FEMALE) bmr -= 161;

        double multiplier = switch (user.getActivityLevel()) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTRA_ACTIVE -> 1.9;
            default -> 1.55;
        };
        
        return bmr * multiplier;
    }
}
