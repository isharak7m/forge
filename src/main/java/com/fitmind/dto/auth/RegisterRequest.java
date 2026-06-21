package com.fitmind.dto.auth;

import com.fitmind.entity.enums.ActivityLevel;
import com.fitmind.entity.enums.FitnessGoal;
import com.fitmind.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Integer age;
    private Gender gender;
    private Double heightCm;
    private Double currentWeightKg;
    private Double goalWeightKg;
    private ActivityLevel activityLevel;
    private FitnessGoal fitnessGoal;
}
