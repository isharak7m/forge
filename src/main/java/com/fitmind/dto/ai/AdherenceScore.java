package com.fitmind.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdherenceScore {
    private double overallScore; // 0-100
    private double workoutConsistency;
    private double nutritionConsistency;
    private double sleepConsistency;
    private String riskLevel; // "LOW", "MEDIUM", "HIGH"
    private String interpretation;
    private List<String> improvementAreas;
}
