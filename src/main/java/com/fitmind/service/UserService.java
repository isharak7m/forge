package com.fitmind.service;

import com.fitmind.dto.user.UpdateProfileRequest;
import com.fitmind.dto.user.UserResponse;
import com.fitmind.entity.User;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getHeightCm() != null) user.setHeightCm(request.getHeightCm());
        if (request.getCurrentWeightKg() != null) user.setCurrentWeightKg(request.getCurrentWeightKg());
        if (request.getGoalWeightKg() != null) user.setGoalWeightKg(request.getGoalWeightKg());
        if (request.getActivityLevel() != null) user.setActivityLevel(request.getActivityLevel());
        if (request.getFitnessGoal() != null) user.setFitnessGoal(request.getFitnessGoal());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .heightCm(user.getHeightCm())
                .currentWeightKg(user.getCurrentWeightKg())
                .goalWeightKg(user.getGoalWeightKg())
                .activityLevel(user.getActivityLevel())
                .fitnessGoal(user.getFitnessGoal())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
