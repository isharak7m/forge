package com.fitmind.controller;

import com.fitmind.ai.assistant.FitnessAssistantService;
import com.fitmind.ai.ml.AdherenceService;
import com.fitmind.ai.ml.PlateauDetectionService;
import com.fitmind.ai.ml.PredictionService;
import com.fitmind.ai.recommendation.RecommendationService;
import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.ai.*;
import com.fitmind.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Intelligence")
public class AIController {

    private final PredictionService predictionService;
    private final PlateauDetectionService plateauService;
    private final AdherenceService adherenceService;
    private final RecommendationService recommendationService;
    private final FitnessAssistantService assistantService;

    @GetMapping("/predict")
    @Operation(summary = "Get weight predictions")
    public ResponseEntity<ApiResponse<WeightPrediction>> getPredictions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(predictionService.predictWeight(user.getId())));
    }

    @GetMapping("/plateaus")
    @Operation(summary = "Get detected plateaus")
    public ResponseEntity<ApiResponse<List<PlateauAlert>>> getPlateaus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(plateauService.detectPlateaus(user.getId())));
    }

    @GetMapping("/adherence")
    @Operation(summary = "Get adherence score")
    public ResponseEntity<ApiResponse<AdherenceScore>> getAdherence(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(adherenceService.calculateAdherence(user.getId())));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get personalized recommendations")
    public ResponseEntity<ApiResponse<List<Recommendation>>> getRecommendations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.generateRecommendations(user.getId())));
    }

    @PostMapping("/assistant")
    @Operation(summary = "Chat with AI Fitness Assistant")
    public ResponseEntity<ApiResponse<AssistantResponse>> askAssistant(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AssistantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(assistantService.processQuery(user.getId(), request.getQuery())));
    }
}
