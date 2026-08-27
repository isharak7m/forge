package com.forge.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WeightPrediction {
    private double currentWeight;
    private double predicted30Days;
    private double predicted60Days;
    private double predicted90Days;
    private String trend; // "GAINING", "LOSING", "STABLE"
    private String confidence; // "HIGH", "MEDIUM", "LOW"
    private String methodology;
    private List<String> keyFactors;
}
