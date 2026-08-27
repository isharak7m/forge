package com.forge.dto.ai;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Recommendation {
    private String category; // "NUTRITION", "TRAINING", "RECOVERY", "ADHERENCE"
    private String title;
    private String description;
    private String reason;
    private String priority; // "HIGH", "MEDIUM", "LOW"
    private String actionItem;
}
