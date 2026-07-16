package com.fitmind.controller;

import com.fitmind.ai.ml.AdherenceService;
import com.fitmind.ai.ml.PlateauDetectionService;
import com.fitmind.ai.ml.PredictionService;
import com.fitmind.ai.ml.RLTrackingService;
import com.fitmind.ai.ml.WorkoutPredictionService;
import com.fitmind.ai.recommendation.RecommendationService;
import com.fitmind.ai.tool.AITool;
import com.fitmind.ai.tool.ToolOrchestrator;
import com.fitmind.ai.tool.ToolRegistry;
import com.fitmind.dto.ApiResponse;
import com.fitmind.dto.ai.AdherenceScore;
import com.fitmind.dto.ai.PlateauAlert;
import com.fitmind.dto.ai.Recommendation;
import com.fitmind.dto.ai.WeightPrediction;
import com.fitmind.dto.ai.WorkoutPrediction;
import com.fitmind.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Intelligence")
public class AIController {

    private final PredictionService predictionService;
    private final PlateauDetectionService plateauService;
    private final AdherenceService adherenceService;
    private final RecommendationService recommendationService;
    private final WorkoutPredictionService workoutPredictionService;
    private final ToolRegistry toolRegistry;
    private final ToolOrchestrator toolOrchestrator;
    private final RLTrackingService rlTrackingService;

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

    @GetMapping("/tools")
    @Operation(summary = "List available AI tools")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listTools() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (var entry : toolRegistry.getAllTools().entrySet()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", entry.getValue().getName());
            info.put("description", entry.getValue().getDescription());
            info.put("parameters", entry.getValue().getParameters());
            result.put(entry.getKey(), info);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/tools/execute")
    @Operation(summary = "Execute a specific AI tool")
    public ResponseEntity<ApiResponse<Map<String, String>>> executeTool(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> request) {
        String toolName = request.get("tool");
        if (toolName == null || toolName.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("tool parameter is required"));
        }
        AITool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unknown tool: " + toolName));
        }
        request.remove("tool");
        String result = tool.execute(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("result", result)));
    }

    @GetMapping("/tools/query")
    @Operation(summary = "Execute tool orchestrator query and return raw tool data")
    public ResponseEntity<ApiResponse<ToolOrchestrator.ToolResult>> queryTools(
            @AuthenticationPrincipal User user,
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(toolOrchestrator.executeQuery(user.getId(), q)));
    }

    @GetMapping("/predict/workout")
    @Operation(summary = "Predict 1RM progression for an exercise")
    public ResponseEntity<ApiResponse<WorkoutPrediction>> predictWorkout(
            @AuthenticationPrincipal User user,
            @RequestParam String exercise) {
        return ResponseEntity.ok(ApiResponse.success(workoutPredictionService.predict1RM(user.getId(), exercise)));
    }

    @GetMapping("/predict/exercises")
    @Operation(summary = "List available exercises for 1RM prediction")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableExercises(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(workoutPredictionService.getAvailableExercises(user.getId())));
    }

    @GetMapping("/rl/recommend")
    @Operation(summary = "Get RL-based recommendation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rlRecommend(@AuthenticationPrincipal User user) {
        var plateaus = plateauService.detectPlateaus(user.getId());
        var adherence = adherenceService.calculateAdherence(user.getId());
        var weightPred = predictionService.predictWeight(user.getId());

        String stateKey = RLTrackingService.buildStateKey(
                adherence.getRiskLevel(),
                weightPred.getTrend(),
                !plateaus.isEmpty()
        );

        String action = rlTrackingService.recommend(stateKey);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("state", stateKey);
        result.put("recommendation", action);
        result.put("qValues", rlTrackingService.getQValues(stateKey));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/rl/feedback")
    @Operation(summary = "Submit RL feedback (reward signal)")
    public ResponseEntity<ApiResponse<String>> rlFeedback(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body) {
        String state = (String) body.get("state");
        String action = (String) body.get("action");
        double reward = ((Number) body.get("reward")).doubleValue();
        String nextState = (String) body.getOrDefault("nextState", state);
        rlTrackingService.update(state, action, reward, nextState);
        return ResponseEntity.ok(ApiResponse.success("Feedback recorded"));
    }
}
