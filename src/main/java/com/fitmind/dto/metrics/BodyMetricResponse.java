package com.fitmind.dto.metrics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BodyMetricResponse {
    private Long id;
    private LocalDate recordedDate;
    private Double weightKg;
    private Double waistCm;
    private Double chestCm;
    private Double armsCm;
    private Double thighsCm;
    private Double bodyFatPercentage;
    private Double sleepHours;
    private Double waterLiters;
    private Integer recoveryScore;
    private String notes;
}
