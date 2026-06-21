package com.fitmind.controller;

import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.nutrition.DailyNutritionSummary;
import com.fitmind.dto.nutrition.FoodLogRequest;
import com.fitmind.dto.nutrition.FoodLogResponse;
import com.fitmind.dto.nutrition.MacroDistribution;
import com.fitmind.entity.User;
import com.fitmind.entity.enums.MealCategory;
import com.fitmind.service.NutritionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @GetMapping("/analytics/macros")
    @Operation(summary = "Get macro distribution")
    public ResponseEntity<ApiResponse<MacroDistribution>> getMacros(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.getMacroDistribution(user.getId(), from, to)));
    }
}
