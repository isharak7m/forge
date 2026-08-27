package com.forge.dto.sleep;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SleepLogRequest {
    private LocalDate date;

    @NotNull(message = "Duration is required")
    @Min(value = 0, message = "Duration must be positive")
    @Max(value = 24, message = "Duration cannot exceed 24 hours")
    private Double durationHours;

    @Min(value = 1, message = "Quality must be between 1 and 10")
    @Max(value = 10, message = "Quality must be between 1 and 10")
    private Integer qualityScore;
}
