package com.fitmind.controller;

import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.workout.ExerciseLogRequest;
import com.fitmind.dto.workout.PersonalRecord;
import com.fitmind.dto.workout.ProgressionPoint;
import com.fitmind.dto.workout.WorkoutSessionRequest;
import com.fitmind.dto.workout.WorkoutSessionResponse;
import com.fitmind.entity.User;
import com.fitmind.service.WorkoutService;
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
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping("/sessions")
    @Operation(summary = "Create workout session")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> createSession(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WorkoutSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.createSession(user.getId(), request)));
    }

    @PostMapping("/sessions/{id}/exercises")
    @Operation(summary = "Add exercise to session")
    public ResponseEntity<ApiResponse<WorkoutSessionResponse>> addExercise(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ExerciseLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.addExercise(user.getId(), id, request)));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get workout sessions")
    public ResponseEntity<ApiResponse<List<WorkoutSessionResponse>>> getSessions(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.getSessions(user.getId(), from, to)));
    }

    @GetMapping("/volume")
    @Operation(summary = "Get volume analytics")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getVolumeAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.getVolumeAnalytics(user.getId(), from, to)));
    }

    @GetMapping("/exercises/prs")
    @Operation(summary = "Get personal records")
    public ResponseEntity<ApiResponse<List<PersonalRecord>>> getPRs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.getPersonalRecords(user.getId())));
    }

    @GetMapping("/frequency")
    @Operation(summary = "Get muscle group frequency")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMuscleFrequency(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.getMuscleGroupFrequency(user.getId(), from, to)));
    }

    @GetMapping("/exercises/{exerciseName}/progression")
    @Operation(summary = "Get exercise progression")
    public ResponseEntity<ApiResponse<List<ProgressionPoint>>> getProgression(
            @AuthenticationPrincipal User user,
            @PathVariable String exerciseName) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.getExerciseProgression(user.getId(), exerciseName)));
    }
}
