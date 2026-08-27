package com.forge.dto.workout;

import com.forge.entity.enums.CardioZone;
import com.forge.entity.enums.ExerciseCategory;
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
