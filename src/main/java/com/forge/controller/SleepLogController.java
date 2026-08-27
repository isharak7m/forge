package com.forge.controller;

import com.forge.dto.ApiResponse;
import com.forge.dto.sleep.SleepLogRequest;
import com.forge.entity.User;
import com.forge.service.SleepLogService;
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
@RequestMapping("/api/sleep")
@RequiredArgsConstructor
@Tag(name = "Sleep Logs")
public class SleepLogController {

    private final SleepLogService sleepLogService;

    @PostMapping
    @Operation(summary = "Log sleep entry")
    public ResponseEntity<ApiResponse<?>> recordSleep(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SleepLogRequest body) {
        LocalDate date = body.getDate() != null ? body.getDate() : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(sleepLogService.recordSleep(user.getId(), date, body.getDurationHours(), body.getQualityScore())));
    }

    @GetMapping("/history")
    @Operation(summary = "Get sleep history for last N days")
    public ResponseEntity<ApiResponse<List<com.forge.entity.SleepLog>>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        return ResponseEntity.ok(ApiResponse.success(sleepLogService.getHistory(user.getId(), from, to)));
    }
}
