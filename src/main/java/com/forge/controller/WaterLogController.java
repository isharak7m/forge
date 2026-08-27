package com.forge.controller;

import com.forge.dto.ApiResponse;
import com.forge.dto.water.WaterLogRequest;
import com.forge.entity.User;
import com.forge.service.WaterLogService;
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
@RequestMapping("/api/water")
@RequiredArgsConstructor
@Tag(name = "Water Logs")
public class WaterLogController {

    private final WaterLogService waterLogService;

    @PostMapping
    @Operation(summary = "Log water intake")
    public ResponseEntity<ApiResponse<?>> recordWater(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WaterLogRequest body) {
        LocalDate date = body.getDate() != null ? body.getDate() : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(waterLogService.recordWater(user.getId(), date, body.getAmountMl())));
    }

    @GetMapping("/history")
    @Operation(summary = "Get water history for last N days")
    public ResponseEntity<ApiResponse<List<com.forge.entity.WaterLog>>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        return ResponseEntity.ok(ApiResponse.success(waterLogService.getHistory(user.getId(), from, to)));
    }
}
