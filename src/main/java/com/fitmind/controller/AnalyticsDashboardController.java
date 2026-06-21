package com.fitmind.controller;

import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.dashboard.AllTimeDashboard;
import com.fitmind.dto.dashboard.DailyDashboard;
import com.fitmind.dto.dashboard.MonthlyDashboard;
import com.fitmind.dto.dashboard.WeeklyDashboard;
import com.fitmind.entity.User;
import com.fitmind.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics/dashboard")
@RequiredArgsConstructor
@Tag(name = "Analytics Dashboard")
public class AnalyticsDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/daily")
    @Operation(summary = "Get daily dashboard")
    public ResponseEntity<ApiResponse<DailyDashboard>> getDailyDashboard(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDailyDashboard(user.getId(), date)));
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly dashboard")
    public ResponseEntity<ApiResponse<WeeklyDashboard>> getWeeklyDashboard(
            @AuthenticationPrincipal User user,
            @RequestParam int week,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getWeeklyDashboard(user.getId(), week, year)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly dashboard")
    public ResponseEntity<ApiResponse<MonthlyDashboard>> getMonthlyDashboard(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthlyDashboard(user.getId(), month, year)));
    }

    @GetMapping("/alltime")
    @Operation(summary = "Get all-time dashboard")
    public ResponseEntity<ApiResponse<AllTimeDashboard>> getAllTimeDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAllTimeDashboard(user.getId())));
    }
}
