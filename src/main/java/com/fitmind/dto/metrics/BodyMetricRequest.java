package com.fitmind.dto.metrics;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BodyMetricRequest {
    private LocalDate recordedDate = LocalDate.now();
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
