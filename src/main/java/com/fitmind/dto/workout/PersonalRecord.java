package com.fitmind.dto.workout;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PersonalRecord {
    private String exerciseName;
    private Double weightKg;
    private Integer reps;
    private Integer sets;
    private Double volume;
    private LocalDate achievedDate;
    private Double estimatedOneRepMax; // Epley formula: weight * (1 + reps/30)
}
