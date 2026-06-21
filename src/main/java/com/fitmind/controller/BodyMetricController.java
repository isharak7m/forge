package com.fitmind.controller;

import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.metrics.BodyMetricRequest;
import com.fitmind.dto.metrics.BodyMetricResponse;
import com.fitmind.dto.metrics.TrendPoint;
import com.fitmind.entity.User;
import com.fitmind.service.BodyMetricService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Body Metrics")
public class BodyMetricController {

    private final BodyMetricService metricService;

    @PostMapping
    @Operation(summary = "Record body metric")
    public ResponseEntity<ApiResponse<BodyMetricResponse>> recordMetric(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BodyMetricRequest request) {
        return ResponseEntity.ok(ApiResponse.success(metricService.recordMetric(user.getId(), request)));
    }

    @GetMapping
    @Operation(summary = "Get metrics history")
    public ResponseEntity<ApiResponse<List<BodyMetricResponse>>> getMetrics(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getMetrics(user.getId(), from, to)));
    }

    @GetMapping("/trends/weight")
    @Operation(summary = "Get weight trend")
    public ResponseEntity<ApiResponse<List<TrendPoint>>> getWeightTrend(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getWeightTrend(user.getId())));
    }

    @GetMapping("/trends/measurements")
    @Operation(summary = "Get measurement trends")
    public ResponseEntity<ApiResponse<Map<String, List<TrendPoint>>>> getMeasurementTrends(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getMeasurementTrends(user.getId())));
    }

    @GetMapping("/trends/sleep")
    @Operation(summary = "Get sleep trend")
    public ResponseEntity<ApiResponse<List<TrendPoint>>> getSleepTrend(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getSleepTrend(user.getId())));
    }

    @GetMapping("/trends/recovery")
    @Operation(summary = "Get recovery trend")
    public ResponseEntity<ApiResponse<List<TrendPoint>>> getRecoveryTrend(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getRecoveryTrend(user.getId())));
    }
}
