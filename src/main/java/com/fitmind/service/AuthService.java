package com.fitmind.service;

import com.fitmind.dto.auth.AuthResponse;
import com.fitmind.dto.auth.LoginRequest;
import com.fitmind.dto.auth.RegisterRequest;
import com.fitmind.dto.user.UserResponse;
import com.fitmind.entity.User;
import com.fitmind.entity.enums.UserRole;
import com.fitmind.exception.AuthException;
import com.fitmind.repository.UserRepository;
import com.fitmind.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already in use.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .gender(request.getGender())
                .heightCm(request.getHeightCm())
                .currentWeightKg(request.getCurrentWeightKg())
                .goalWeightKg(request.getGoalWeightKg())
                .activityLevel(request.getActivityLevel())
                .fitnessGoal(request.getFitnessGoal())
                .role(UserRole.USER) // Default role
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(user);

            return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
        } catch (AuthenticationException e) {
             throw new AuthException("Invalid email or password");
        }
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
