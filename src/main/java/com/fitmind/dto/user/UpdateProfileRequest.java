package com.fitmind.dto.user;

import com.fitmind.entity.enums.ActivityLevel;
import com.fitmind.entity.enums.FitnessGoal;
import com.fitmind.entity.enums.Gender;
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
