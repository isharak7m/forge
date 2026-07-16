package com.fitmind.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPrediction {
    private String exerciseName;
    private double current1RM;
    private double predicted30Days1RM;
    private double predicted60Days1RM;
    private double predicted90Days1RM;
    private String trend; // GAINING, LOSING, STABLE
    private String confidence; // HIGH, MEDIUM, LOW
    private String methodology;
    private List<String> keyFactors;
}
