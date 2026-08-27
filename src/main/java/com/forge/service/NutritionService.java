package com.forge.service;

import com.forge.dto.nutrition.DailyNutritionSummary;
import com.forge.dto.nutrition.FoodLogRequest;
import com.forge.dto.nutrition.FoodLogResponse;
import com.forge.dto.nutrition.MacroDistribution;
import com.forge.entity.FoodLog;
import com.forge.entity.User;
import com.forge.entity.enums.MealCategory;
import com.forge.exception.AuthException;
import com.forge.exception.ResourceNotFoundException;
import com.forge.repository.FoodLogRepository;
import com.forge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final FoodLogRepository foodLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public FoodLogResponse logFood(Long userId, FoodLogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FoodLog foodLog = FoodLog.builder()
                .user(user)
                .foodName(request.getFoodName())
                .mealCategory(request.getMealCategory())
                .date(request.getDate())
                .servingSize(request.getServingSize())
                .unit(request.getUnit())
                .calories(request.getCalories())
                .proteinG(request.getProteinG())
                .carbsG(request.getCarbsG())
                .fatG(request.getFatG())
                .fiberG(request.getFiberG())
                .vitaminA(request.getVitaminA())
                .vitaminC(request.getVitaminC())
                .vitaminD(request.getVitaminD())
                .vitaminE(request.getVitaminE())
                .vitaminK(request.getVitaminK())
                .vitaminB1(request.getVitaminB1())
                .vitaminB2(request.getVitaminB2())
                .vitaminB3(request.getVitaminB3())
                .vitaminB5(request.getVitaminB5())
                .vitaminB6(request.getVitaminB6())
                .vitaminB7(request.getVitaminB7())
                .vitaminB9(request.getVitaminB9())
                .vitaminB12(request.getVitaminB12())
                .calcium(request.getCalcium())
                .iron(request.getIron())
                .magnesium(request.getMagnesium())
                .potassium(request.getPotassium())
                .sodium(request.getSodium())
                .zinc(request.getZinc())
                .copper(request.getCopper())
                .manganese(request.getManganese())
                .selenium(request.getSelenium())
                .phosphorus(request.getPhosphorus())
                .iodine(request.getIodine())
                .chromium(request.getChromium())
                .build();

        FoodLog saved = foodLogRepository.save(foodLog);
        return mapToResponse(saved);
    }

    public List<FoodLogResponse> getLogs(Long userId, LocalDate date, MealCategory category) {
        List<FoodLog> logs;
        if (category != null) {
            logs = foodLogRepository.findByUserIdAndDateAndMealCategory(userId, date, category);
        } else {
            logs = foodLogRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, date);
        }
        return logs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteLog(Long userId, Long logId) {
        FoodLog log = foodLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Food log not found"));

        if (!log.getUser().getId().equals(userId)) {
            throw new AuthException("Unauthorized to delete this log");
        }

        foodLogRepository.delete(log);
    }

    public DailyNutritionSummary getDailyAnalytics(Long userId, LocalDate date) {
        List<FoodLog> logs = foodLogRepository.findByUserIdAndDateOrderByLoggedAtAsc(userId, date);
        
        double totalCalories = logs.stream().mapToDouble(l -> l.getCalories() != null ? l.getCalories() : 0.0).sum();
        double totalProtein = logs.stream().mapToDouble(l -> l.getProteinG() != null ? l.getProteinG() : 0.0).sum();
        double totalCarbs = logs.stream().mapToDouble(l -> l.getCarbsG() != null ? l.getCarbsG() : 0.0).sum();
        double totalFat = logs.stream().mapToDouble(l -> l.getFatG() != null ? l.getFatG() : 0.0).sum();
        double totalFiber = logs.stream().mapToDouble(l -> l.getFiberG() != null ? l.getFiberG() : 0.0).sum();

        Map<String, Double> micros = new LinkedHashMap<>();
        String[] microFields = {"vitaminA","vitaminC","vitaminD","vitaminE","vitaminK",
            "vitaminB1","vitaminB2","vitaminB3","vitaminB5","vitaminB6","vitaminB7","vitaminB9","vitaminB12",
            "calcium","iron","magnesium","potassium","sodium","zinc","copper","manganese","selenium",
            "phosphorus","iodine","chromium"};
        for (String field : microFields) {
            double sum = 0;
            for (FoodLog l : logs) {
                Double val = switch (field) {
                    case "vitaminA" -> l.getVitaminA();
                    case "vitaminC" -> l.getVitaminC();
                    case "vitaminD" -> l.getVitaminD();
                    case "vitaminE" -> l.getVitaminE();
                    case "vitaminK" -> l.getVitaminK();
                    case "vitaminB1" -> l.getVitaminB1();
                    case "vitaminB2" -> l.getVitaminB2();
                    case "vitaminB3" -> l.getVitaminB3();
                    case "vitaminB5" -> l.getVitaminB5();
                    case "vitaminB6" -> l.getVitaminB6();
                    case "vitaminB7" -> l.getVitaminB7();
                    case "vitaminB9" -> l.getVitaminB9();
                    case "vitaminB12" -> l.getVitaminB12();
                    case "calcium" -> l.getCalcium();
                    case "iron" -> l.getIron();
                    case "magnesium" -> l.getMagnesium();
                    case "potassium" -> l.getPotassium();
                    case "sodium" -> l.getSodium();
                    case "zinc" -> l.getZinc();
                    case "copper" -> l.getCopper();
                    case "manganese" -> l.getManganese();
                    case "selenium" -> l.getSelenium();
                    case "phosphorus" -> l.getPhosphorus();
                    case "iodine" -> l.getIodine();
                    case "chromium" -> l.getChromium();
                    default -> 0.0;
                };
                if (val != null) sum += val;
            }
            micros.put(field, Math.round(sum * 100.0) / 100.0);
        }

        List<FoodLogResponse> meals = logs.stream().map(this::mapToResponse).collect(Collectors.toList());
        
        return DailyNutritionSummary.builder()
                .date(date)
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarbs(totalCarbs)
                .totalFat(totalFat)
                .totalFiber(totalFiber)
                .consistencyScore(calculateConsistencyScore(logs))
                .meals(meals)
                .micronutrients(micros)
                .build();
    }

    public List<DailyNutritionSummary> getWeeklyAnalytics(Long userId, int week, int year) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
        LocalDate from = firstDayOfYear.with(weekFields.weekOfYear(), Math.min(week, 52)).with(DayOfWeek.MONDAY);
        LocalDate to = from.plusDays(6);
        if (to.isAfter(LocalDate.now())) {
            to = LocalDate.now();
        }
        return getAnalyticsForRange(userId, from, to);
    }

    public List<DailyNutritionSummary> getMonthlyAnalytics(Long userId, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        if (to.isAfter(LocalDate.now())) {
            to = LocalDate.now();
        }
        return getAnalyticsForRange(userId, from, to);
    }

    private List<DailyNutritionSummary> getAnalyticsForRange(Long userId, LocalDate from, LocalDate to) {
        List<DailyNutritionSummary> summaries = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            summaries.add(getDailyAnalytics(userId, current));
            current = current.plusDays(1);
        }
        return summaries;
    }

    public MacroDistribution getMacroDistribution(Long userId, LocalDate from, LocalDate to) {
        Double totalCalories = foodLogRepository.sumCaloriesByUserIdAndDateBetween(userId, from, to);
        Double totalProtein = foodLogRepository.sumProteinByUserIdAndDateBetween(userId, from, to);
        Double totalCarbs = foodLogRepository.sumCarbsByUserIdAndDateBetween(userId, from, to);
        Double totalFat = foodLogRepository.sumFatByUserIdAndDateBetween(userId, from, to);

        totalCalories = totalCalories != null ? totalCalories : 0.0;
        totalProtein = totalProtein != null ? totalProtein : 0.0;
        totalCarbs = totalCarbs != null ? totalCarbs : 0.0;
        totalFat = totalFat != null ? totalFat : 0.0;

        double proteinCals = totalProtein * 4;
        double carbCals = totalCarbs * 4;
        double fatCals = totalFat * 9;
        
        double calcTotalCals = proteinCals + carbCals + fatCals;
        
        double pPct = calcTotalCals > 0 ? (proteinCals / calcTotalCals) * 100 : 0;
        double cPct = calcTotalCals > 0 ? (carbCals / calcTotalCals) * 100 : 0;
        double fPct = calcTotalCals > 0 ? (fatCals / calcTotalCals) * 100 : 0;

        return MacroDistribution.builder()
                .totalCalories(totalCalories)
                .totalProtein(totalProtein)
                .totalCarbs(totalCarbs)
                .totalFat(totalFat)
                .proteinCalories(proteinCals)
                .carbCalories(carbCals)
                .fatCalories(fatCals)
                .proteinPct(pPct)
                .carbsPct(cPct)
                .fatPct(fPct)
                .build();
    }

    private FoodLogResponse mapToResponse(FoodLog log) {
        return FoodLogResponse.builder()
                .id(log.getId())
                .date(log.getDate())
                .mealCategory(log.getMealCategory())
                .foodName(log.getFoodName())
                .servingSize(log.getServingSize())
                .unit(log.getUnit())
                .calories(log.getCalories())
                .proteinG(log.getProteinG())
                .carbsG(log.getCarbsG())
                .fatG(log.getFatG())
                .fiberG(log.getFiberG())
                .vitaminA(log.getVitaminA())
                .vitaminC(log.getVitaminC())
                .vitaminD(log.getVitaminD())
                .vitaminE(log.getVitaminE())
                .vitaminK(log.getVitaminK())
                .vitaminB1(log.getVitaminB1())
                .vitaminB2(log.getVitaminB2())
                .vitaminB3(log.getVitaminB3())
                .vitaminB5(log.getVitaminB5())
                .vitaminB6(log.getVitaminB6())
                .vitaminB7(log.getVitaminB7())
                .vitaminB9(log.getVitaminB9())
                .vitaminB12(log.getVitaminB12())
                .calcium(log.getCalcium())
                .iron(log.getIron())
                .magnesium(log.getMagnesium())
                .potassium(log.getPotassium())
                .sodium(log.getSodium())
                .zinc(log.getZinc())
                .copper(log.getCopper())
                .manganese(log.getManganese())
                .selenium(log.getSelenium())
                .phosphorus(log.getPhosphorus())
                .iodine(log.getIodine())
                .chromium(log.getChromium())
                .loggedAt(log.getLoggedAt())
                .build();
    }

    private double calculateConsistencyScore(List<FoodLog> logs) {
        if (logs.isEmpty()) return 0.0;
        Set<MealCategory> categories = logs.stream()
                .map(FoodLog::getMealCategory)
                .collect(Collectors.toSet());
        double targetMeals = 4.0;
        return Math.min(100.0, (categories.size() / targetMeals) * 100.0);
    }
}
