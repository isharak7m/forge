package com.forge.dto.user;

import com.forge.entity.enums.ActivityLevel;
import com.forge.entity.enums.FitnessGoal;
import com.forge.entity.enums.Gender;
import com.forge.entity.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private Gender gender;
    private Double heightCm;
    private Double currentWeightKg;
    private Double goalWeightKg;
    private ActivityLevel activityLevel;
    private FitnessGoal fitnessGoal;
    private UserRole role;
    private LocalDateTime createdAt;
}
