package com.forge.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PlateauAlert {
    private String type; // "WEIGHT", "STRENGTH", "TRAINING_VOLUME"
    private String description;
    private int daysStagnant;
    private String affectedMetric;
    private String severity; // "LOW", "MEDIUM", "HIGH"
    private LocalDate detectedSince;
}
