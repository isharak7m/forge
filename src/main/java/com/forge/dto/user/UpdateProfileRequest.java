package com.forge.dto.user;

import com.forge.entity.enums.ActivityLevel;
import com.forge.entity.enums.FitnessGoal;
import com.forge.entity.enums.Gender;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private Integer age;
    private Gender gender;
    private Double heightCm;
    private Double currentWeightKg;
    private Double goalWeightKg;
    private ActivityLevel activityLevel;
    private FitnessGoal fitnessGoal;
}
