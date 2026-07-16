package com.fitmind.controller;

import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.weight.WeightLogRequest;
import com.fitmind.entity.User;
import com.fitmind.entity.weight.WeightLog;
import com.fitmind.service.WeightLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weight")
@RequiredArgsConstructor
@Tag(name = "Weight Logs")
public class WeightController {

    private final WeightLogService weightLogService;

    @PostMapping
    @Operation(summary = "Log body weight")
    public ResponseEntity<ApiResponse<WeightLog>> recordWeight(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WeightLogRequest body) {
        LocalDate date = body.getDate() != null ? body.getDate() : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(weightLogService.recordWeight(user.getId(), date, body.getWeightKg())));
    }

    @GetMapping("/history")
    @Operation(summary = "Get weight history for last N days")
    public ResponseEntity<ApiResponse<List<WeightLog>>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(weightLogService.getHistory(user.getId(), days)));
    }
}
