package com.fitmind.dto.workout;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutSessionRequest {
    private LocalDate date = LocalDate.now();
    @NotBlank(message = "Workout name is required")
    private String name;
    private String notes;
    private Integer durationMinutes;
}
