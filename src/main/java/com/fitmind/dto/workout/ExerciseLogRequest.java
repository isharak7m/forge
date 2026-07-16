package com.fitmind.dto.workout;

import com.fitmind.entity.enums.CardioZone;
import com.fitmind.entity.enums.ExerciseCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExerciseLogRequest {
    @NotBlank(message = "Exercise name is required")
    private String exerciseName;
    private ExerciseCategory category;
    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private Integer rpe;
    private Integer duration;
    private CardioZone zone;
    private String notes;
}
