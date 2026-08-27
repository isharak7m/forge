package com.forge.dto.workout;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkoutSessionResponse {
    private Long id;
    private LocalDate date;
    private String name;
    private String notes;
    private Integer durationMinutes;
    private List<ExerciseLogResponse> exercises;
    private Double totalVolume;
    private LocalDateTime createdAt;
}
