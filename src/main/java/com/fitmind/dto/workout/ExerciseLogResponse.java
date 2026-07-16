package com.fitmind.dto.workout;

import com.fitmind.entity.enums.CardioZone;
import com.fitmind.entity.enums.ExerciseCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseLogResponse {
    private Long id;
    private String exerciseName;
    private ExerciseCategory category;
    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private Integer rpe;
    private Integer duration;
    private CardioZone zone;
    private String notes;
    private Double volume;
}
