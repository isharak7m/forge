package com.forge.config;

import com.forge.entity.*;
import com.forge.entity.enums.*;
import com.forge.entity.weight.WeightLog;
import com.forge.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_EMAIL = "demo@forge.com";
    private static final String DEMO_PASSWORD = "Demo@123";

    private final UserRepository userRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExerciseLogRepository exerciseLogRepository;
    private final FoodLogRepository foodLogRepository;
    private final SleepLogRepository sleepLogRepository;
    private final WaterLogRepository waterLogRepository;
    private final WeightLogRepository weightLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    private final Random rng = new Random(42);

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            User existing = userRepository.findByEmail(DEMO_EMAIL).orElseThrow();
            long sessionCount = workoutSessionRepository.countByUserIdAndDateBetween(
                    existing.getId(), LocalDate.now().minusDays(180), LocalDate.now());
            if (sessionCount >= 80) {
                log.info("Demo user already has {} sessions (>=80), skipping seed.", sessionCount);
                long weightCount = weightLogRepository.countByUserId(existing.getId());
                if (weightCount < 80) {
                    log.info("But only {} weight logs found, seeding weight logs.", weightCount);
                    seedWeightLogs(existing);
                    log.info("Weight logs seeded.");
                }
                extendDemoDataToToday(existing);
                return;
            }
            log.info("Demo user exists but only {} sessions found, reseeding with full data.", sessionCount);
            exerciseLogRepository.deleteAll(
                    exerciseLogRepository.findAllByUserId(existing.getId()));
            workoutSessionRepository.deleteAll(
                    workoutSessionRepository.findByUserIdOrderByDateDesc(existing.getId()));
            foodLogRepository.deleteAll(
                    foodLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(existing.getId(),
                            LocalDate.now().minusDays(180), LocalDate.now()));
            sleepLogRepository.deleteAll(
                    sleepLogRepository.findByUserId(existing.getId()));
            waterLogRepository.deleteAll(
                    waterLogRepository.findByUserId(existing.getId()));
            weightLogRepository.deleteAll(
                    weightLogRepository.findByUserIdOrderByDateAsc(existing.getId()));
            userRepository.delete(existing);
            entityManager.flush();
            entityManager.clear();
        }

        log.info("Seeding 6 months of demo data...");
        User user = createDemoUser();
        seedWorkouts(user);
        seedFoodLogs(user);
        seedSleepLogs(user);
        seedWaterLogs(user);
        seedWeightLogs(user);
        log.info("Demo data seeding complete.");
    }

    private User createDemoUser() {
        User user = User.builder()
                .name("Alex Johnson")
                .email(DEMO_EMAIL)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .age(28)
                .gender(Gender.MALE)
                .heightCm(178.0)
                .currentWeightKg(79.5)
                .goalWeightKg(76.0)
                .activityLevel(ActivityLevel.VERY_ACTIVE)
                .fitnessGoal(FitnessGoal.MUSCLE_GAIN)
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    // ── Workouts: 24 weeks, ending today ──────────────────────────

    private void seedWorkouts(User user) {
        int totalWeeks = 24;
        LocalDate startDate = LocalDate.now().minusDays(totalWeeks * 7L);

        for (int week = 0; week < totalWeeks; week++) {
            double f = week; // progressive overload factor

            // Monday: Push A
            LocalDate mon = startDate.plusDays(week * 7L);
            if (!mon.isAfter(LocalDate.now())) {
                createSession(user, mon, "Push A", 55, List.of(
                        exercise("Bench Press", ExerciseCategory.STRENGTH, 3, 8 + week % 2,
                                60.0 + f * 2.5, 7 + week % 2),
                        exercise("Overhead Press", ExerciseCategory.STRENGTH, 3, 8,
                                35.0 + f * 1.5, 7 + (week % 3)),
                        exercise("Triceps Pushdown", ExerciseCategory.STRENGTH, 3, 12,
                                20.0 + f * 1.25, 7 + (week % 3))
                ));
            }

            // Tuesday: Pull
            LocalDate tue = startDate.plusDays(week * 7L + 1);
            if (!tue.isAfter(LocalDate.now())) {
                createSession(user, tue, "Pull", 50, List.of(
                        exercise("Barbell Row", ExerciseCategory.STRENGTH, 3, 8,
                                50.0 + f * 2.5, 7 + week % 2),
                        exercise("Lat Pulldown", ExerciseCategory.STRENGTH, 3, 10 + week % 2,
                                55.0 + f * 2.0, 7 + (week % 3)),
                        exercise("Bicep Curl", ExerciseCategory.STRENGTH, 3, 12,
                                15.0 + f * 1.0, 8)
                ));
            }

            // Thursday: Push B
            LocalDate thu = startDate.plusDays(week * 7L + 3);
            if (!thu.isAfter(LocalDate.now())) {
                createSession(user, thu, "Push B", 50, List.of(
                        exercise("Incline Bench Press", ExerciseCategory.STRENGTH, 3, 8 + week % 2,
                                50.0 + f * 2.5, 7 + week % 2),
                        exercise("Overhead Press", ExerciseCategory.STRENGTH, 3, 7 + week % 2,
                                35.0 + f * 1.5, 7 + (week % 3)),
                        exercise("Dips", ExerciseCategory.STRENGTH, 3, 8 + week % 2,
                                75.0, 7 + (week % 2))
                ));
            }

            // Friday: Legs
            LocalDate fri = startDate.plusDays(week * 7L + 4);
            if (!fri.isAfter(LocalDate.now())) {
                createSession(user, fri, "Legs", 60, List.of(
                        exercise("Squat", ExerciseCategory.STRENGTH, 3, 8,
                                80.0 + f * 3.0, 7 + week % 2),
                        exercise("Deadlift", ExerciseCategory.STRENGTH, 3, 6 + week % 2,
                                100.0 + f * 4.0, 7 + (week % 3)),
                        exercise("Leg Press", ExerciseCategory.STRENGTH, 3, 10,
                                120.0 + f * 7.5, 7 + (week % 3))
                ));
            }
        }
    }

    private ExerciseLog exercise(String name, ExerciseCategory cat, int sets, int reps,
                                 double weightKg, int rpe) {
        return ExerciseLog.builder()
                .exerciseName(name)
                .category(cat)
                .sets(sets)
                .reps(reps)
                .weightKg(weightKg)
                .rpe(rpe)
                .build();
    }

    private void createSession(User user, LocalDate date, String name, int durationMinutes,
                               List<ExerciseLog> exercises) {
        WorkoutSession session = WorkoutSession.builder()
                .user(user)
                .date(date)
                .name(name + " - Week " + ((int) ChronoUnit.WEEKS.between(
                        LocalDate.now().minusDays(24 * 7L), date) + 1))
                .durationMinutes(durationMinutes)
                .build();
        session = workoutSessionRepository.save(session);

        WorkoutSession finalSession = session;
        exercises.forEach(el -> {
            el.setWorkoutSession(finalSession);
            exerciseLogRepository.save(el);
        });
    }

    // ── Food logs: 90 days, ending today ──────────────────────────

    private void seedFoodLogs(User user) {
        int days = 90;
        LocalDate start = LocalDate.now().minusDays(days - 1);

        String[][] breakfasts = {
                {"Oatmeal with Whey Protein", "1", "BOWL", "420", "35", "45", "8", "5"},
                {"Scrambled Eggs & Avocado Toast", "2", "SLICE", "450", "28", "35", "22", "6"},
                {"Greek Yogurt Parfait", "1", "SERVING", "380", "25", "40", "12", "4"},
                {"Protein Pancakes", "3", "PIECE", "430", "32", "50", "10", "3"},
                {"Smoothie Bowl", "1", "BOWL", "400", "30", "55", "8", "7"},
        };
        String[][] lunches = {
                {"Chicken Breast with Rice", "200", "GRAM", "550", "48", "60", "10", "2"},
                {"Grilled Salmon Salad", "1", "SERVING", "480", "40", "15", "28", "5"},
                {"Turkey Sandwich", "1", "SERVING", "520", "38", "55", "14", "4"},
                {"Beef Stir-fry with Noodles", "250", "GRAM", "600", "42", "55", "18", "3"},
                {"Quinoa Buddha Bowl", "1", "BOWL", "460", "22", "55", "16", "8"},
        };
        String[][] dinners = {
                {"Salmon with Sweet Potato", "200", "GRAM", "620", "42", "50", "22", "6"},
                {"Lean Steak & Roasted Veggies", "200", "GRAM", "580", "50", "30", "25", "5"},
                {"Grilled Chicken Pasta", "250", "GRAM", "650", "45", "65", "18", "4"},
                {"Baked Cod with Couscous", "200", "GRAM", "520", "40", "55", "12", "3"},
                {"Lean Turkey Chilli", "1", "BOWL", "490", "44", "40", "16", "7"},
        };
        String[][] snacks = {
                {"Greek Yogurt with Almonds", "1", "SERVING", "280", "20", "15", "14", "3"},
                {"Protein Bar", "1", "PIECE", "240", "20", "25", "8", "2"},
                {"Apple with Peanut Butter", "1", "SERVING", "260", "8", "30", "14", "5"},
                {"Cottage Cheese & Berries", "1", "SERVING", "220", "24", "18", "6", "2"},
                {"Rice Cakes with Avocado", "2", "PIECE", "230", "6", "28", "12", "4"},
        };

        for (int day = 0; day < days; day++) {
            LocalDate d = start.plusDays(day);
            int idx = day % 5;
            foodLogRepository.saveAll(List.of(
                    makeMeal(user, d, MealCategory.BREAKFAST, breakfasts[idx]),
                    makeMeal(user, d, MealCategory.LUNCH, lunches[idx]),
                    makeMeal(user, d, MealCategory.DINNER, dinners[idx]),
                    makeMeal(user, d, MealCategory.SNACK, snacks[idx])
            ));
        }
    }

    private FoodLog makeMeal(User user, LocalDate date, MealCategory cat, String[] data) {
        return FoodLog.builder()
                .user(user).date(date).mealCategory(cat)
                .foodName(data[0])
                .servingSize(Double.parseDouble(data[1]))
                .unit(ServingUnit.valueOf(data[2]))
                .calories(Double.parseDouble(data[3]))
                .proteinG(Double.parseDouble(data[4]))
                .carbsG(Double.parseDouble(data[5]))
                .fatG(Double.parseDouble(data[6]))
                .fiberG(Double.parseDouble(data[7]))
                .build();
    }

    // ── Sleep logs: 90 days, ending today ──────────────────────────

    private void seedSleepLogs(User user) {
        int days = 90;
        LocalDate start = LocalDate.now().minusDays(days - 1);
        for (int day = 0; day < days; day++) {
            LocalDate d = start.plusDays(day);
            double hours = 7.0 + rng.nextDouble() * 1.8;
            int quality = 6 + rng.nextInt(4);
            int bedtimeMin = Math.min(rng.nextInt(45), 59);
            sleepLogRepository.save(SleepLog.builder()
                    .user(user).date(d)
                    .durationHours(Math.round(hours * 10.0) / 10.0)
                    .qualityScore(quality)
                    .bedtime(LocalTime.of(22, bedtimeMin))
                    .wakeTime(LocalTime.of(6, rng.nextInt(30)))
                    .build());
        }
    }

    // ── Water logs: 90 days, ending today ──────────────────────────

    private void seedWaterLogs(User user) {
        int days = 90;
        LocalDate start = LocalDate.now().minusDays(days - 1);
        for (int day = 0; day < days; day++) {
            LocalDate d = start.plusDays(day);
            double ml = 2000.0 + rng.nextDouble() * 1200.0;
            waterLogRepository.save(WaterLog.builder()
                    .user(user).date(d)
                    .amountMl(Math.round(ml / 100.0) * 100.0)
                    .build());
        }
    }

    private void seedWeightLogs(User user) {
        int days = 90;
        LocalDate start = LocalDate.now().minusDays(days - 1);
        double weight = 82.0;
        for (int day = 0; day < days; day++) {
            LocalDate d = start.plusDays(day);
            double dailyFluctuation = (rng.nextDouble() - 0.5) * 0.6;
            weight = weight - 0.018 + dailyFluctuation * 0.1;
            weight = Math.round(weight * 10.0) / 10.0;
            weightLogRepository.save(WeightLog.builder()
                    .user(user).date(d)
                    .weightKg(Math.max(weight, 70.0))
                    .build());
        }
    }

    private void extendDemoDataToToday(User user) {
        java.util.Optional<LocalDate> lastFoodDate = foodLogRepository.findMaxDateByUserId(user.getId());
        if (lastFoodDate.isEmpty() || lastFoodDate.get().isEqual(LocalDate.now())) {
            return;
        }
        LocalDate lastDate = lastFoodDate.get();
        LocalDate today = LocalDate.now();
        long missingDays = ChronoUnit.DAYS.between(lastDate, today);
        if (missingDays <= 0) return;
        log.info("Extending demo data by {} days from {} to {}", missingDays, lastDate.plusDays(1), today);

        String[][] breakfasts = {
                {"Oatmeal with Whey Protein", "1", "BOWL", "420", "35", "45", "8", "5"},
                {"Scrambled Eggs & Avocado Toast", "2", "SLICE", "450", "28", "35", "22", "6"},
                {"Greek Yogurt Parfait", "1", "SERVING", "380", "25", "40", "12", "4"},
                {"Protein Pancakes", "3", "PIECE", "430", "32", "50", "10", "3"},
                {"Smoothie Bowl", "1", "BOWL", "400", "30", "55", "8", "7"},
        };
        String[][] lunches = {
                {"Chicken Breast with Rice", "200", "GRAM", "550", "48", "60", "10", "2"},
                {"Grilled Salmon Salad", "1", "SERVING", "480", "40", "15", "28", "5"},
                {"Turkey Sandwich", "1", "SERVING", "520", "38", "55", "14", "4"},
                {"Beef Stir-fry with Noodles", "250", "GRAM", "600", "42", "55", "18", "3"},
                {"Quinoa Buddha Bowl", "1", "BOWL", "460", "22", "55", "16", "8"},
        };
        String[][] dinners = {
                {"Salmon with Sweet Potato", "200", "GRAM", "620", "42", "50", "22", "6"},
                {"Lean Steak & Roasted Veggies", "200", "GRAM", "580", "50", "30", "25", "5"},
                {"Grilled Chicken Pasta", "250", "GRAM", "650", "45", "65", "18", "4"},
                {"Baked Cod with Couscous", "200", "GRAM", "520", "40", "55", "12", "3"},
                {"Lean Turkey Chilli", "1", "BOWL", "490", "44", "40", "16", "7"},
        };
        String[][] snacks = {
                {"Greek Yogurt with Almonds", "1", "SERVING", "280", "20", "15", "14", "3"},
                {"Protein Bar", "1", "PIECE", "240", "20", "25", "8", "2"},
                {"Apple with Peanut Butter", "1", "SERVING", "260", "8", "30", "14", "5"},
                {"Cottage Cheese & Berries", "1", "SERVING", "220", "24", "18", "6", "2"},
                {"Rice Cakes with Avocado", "2", "PIECE", "230", "6", "28", "12", "4"},
        };

        int startIdx = lastDate.getDayOfYear() % 5;
        for (long offset = 1; offset <= missingDays; offset++) {
            LocalDate d = lastDate.plusDays(offset);
            int idx = (startIdx + (int) offset - 1) % 5;
            foodLogRepository.saveAll(List.of(
                    makeMeal(user, d, MealCategory.BREAKFAST, breakfasts[idx]),
                    makeMeal(user, d, MealCategory.LUNCH, lunches[idx]),
                    makeMeal(user, d, MealCategory.DINNER, dinners[idx]),
                    makeMeal(user, d, MealCategory.SNACK, snacks[idx])
            ));
            double hours = 7.0 + rng.nextDouble() * 1.8;
            int quality = 6 + rng.nextInt(4);
            int bedtimeMin = Math.min(rng.nextInt(45), 59);
            sleepLogRepository.save(SleepLog.builder()
                    .user(user).date(d)
                    .durationHours(Math.round(hours * 10.0) / 10.0)
                    .qualityScore(quality)
                    .bedtime(LocalTime.of(22, bedtimeMin))
                    .wakeTime(LocalTime.of(6, rng.nextInt(30)))
                    .build());
            double ml = 2000.0 + rng.nextDouble() * 1200.0;
            waterLogRepository.save(WaterLog.builder()
                    .user(user).date(d)
                    .amountMl(Math.round(ml / 100.0) * 100.0)
                    .build());
        }
        log.info("Demo data extension complete.");
    }
}
