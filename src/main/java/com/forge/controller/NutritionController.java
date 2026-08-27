package com.forge.controller;

import com.forge.dto.ApiResponse;
import com.forge.dto.nutrition.DailyNutritionSummary;
import com.forge.dto.nutrition.FoodLogRequest;
import com.forge.dto.nutrition.FoodLogResponse;
import com.forge.dto.nutrition.MacroDistribution;
import com.forge.entity.User;
import com.forge.entity.enums.MealCategory;
import com.forge.service.NutritionService;
import com.forge.service.LocalFoodDatabaseService;
import com.forge.dto.nutrition.FoodDatabaseItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
@Tag(name = "Nutrition")
public class NutritionController {

    private final NutritionService nutritionService;
    private final LocalFoodDatabaseService localFoodDatabaseService;

    @GetMapping("/search")
    @Operation(summary = "Search local food database")
    public ResponseEntity<ApiResponse<List<FoodDatabaseItem>>> searchFoods(
            @AuthenticationPrincipal User user,
            @RequestParam @Size(max = 100) String query,
            @RequestParam(defaultValue = "20") int limit) {
        if (query.trim().length() > 100) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Search query too long (max 100 characters)"));
        }
        return ResponseEntity.ok(ApiResponse.success(localFoodDatabaseService.searchFoods(query, limit)));
    }

    @PostMapping("/log")
    @Operation(summary = "Log food")
    public ResponseEntity<ApiResponse<FoodLogResponse>> logFood(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FoodLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.logFood(user.getId(), request)));
    }

    @GetMapping("/logs")
    @Operation(summary = "Get food logs")
    public ResponseEntity<ApiResponse<List<FoodLogResponse>>> getLogs(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) MealCategory category) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.getLogs(user.getId(), date, category)));
    }

    @DeleteMapping("/log/{id}")
    @Operation(summary = "Delete food log")
    public ResponseEntity<ApiResponse<Void>> deleteLog(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        nutritionService.deleteLog(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Log deleted successfully", null));
    }

    @GetMapping("/analytics/daily")
    @Operation(summary = "Get daily nutrition analytics")
    public ResponseEntity<ApiResponse<DailyNutritionSummary>> getDailyAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.getDailyAnalytics(user.getId(), date)));
    }

    @GetMapping("/analytics/weekly")
    @Operation(summary = "Get weekly nutrition analytics")
    public ResponseEntity<ApiResponse<List<DailyNutritionSummary>>> getWeeklyAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam int week,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.getWeeklyAnalytics(user.getId(), week, year)));
    }

    @GetMapping("/analytics/monthly")
    @Operation(summary = "Get monthly nutrition analytics")
    public ResponseEntity<ApiResponse<List<DailyNutritionSummary>>> getMonthlyAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.getMonthlyAnalytics(user.getId(), month, year)));
    }

    @GetMapping("/search/nlp")
    @Operation(summary = "Natural language food search")
    public ResponseEntity<ApiResponse<List<FoodDatabaseItem>>> searchFoodsNLP(
            @AuthenticationPrincipal User user,
            @RequestParam @Size(max = 200) String q) {
        String clean = q.toLowerCase()
                .replaceAll("(?i)^(what should |what can |how can |how should |where can |i want |i need |i ate |i eat |show me |find me |give me |what are |what is |what do |list |some |can you |could you |would you |tell me )+", "")
                .replaceAll("(?i)(high in |rich in |good source of |high |low in |low )+", "")
                .replaceAll("(?i)\\b(for|with|that|are|have|under|over|and|the|a|an|of|to|my|me|all|any|i|ideas?|options?|please|want|need|would|like|some|what|which|best|great|good|top|should|can|how|where|who|why|do|does|did|is|am|are|was|were|be|been|being|have|has|had|eat|eating|ate|eaten|get|getting|got|make|making|made)\\b", " ")
                .trim();
        clean = clean.replaceAll("\\s+", " ").trim();
        if (clean.length() < 2) return ResponseEntity.ok(ApiResponse.success(localFoodDatabaseService.searchFoods(q, 15)));

        String[] keywords = clean.split("\\s+");
        java.util.LinkedHashSet<FoodDatabaseItem> results = new java.util.LinkedHashSet<>();
        for (String kw : keywords) {
            if (kw.length() >= 3) {
                results.addAll(localFoodDatabaseService.searchFoods(kw, 5));
            }
        }
        if (results.isEmpty()) {
            results.addAll(localFoodDatabaseService.searchFoods(clean, 15));
        }
        List<FoodDatabaseItem> sorted = new java.util.ArrayList<>(results);
        return ResponseEntity.ok(ApiResponse.success(sorted.subList(0, Math.min(sorted.size(), 20))));
    }

    @GetMapping("/analytics/macros")
    @Operation(summary = "Get macro distribution")
    public ResponseEntity<ApiResponse<MacroDistribution>> getMacros(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.getMacroDistribution(user.getId(), from, to)));
    }
}
