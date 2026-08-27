package com.forge.ai.ml;

import com.forge.entity.ExerciseLog;
import com.forge.entity.User;
import com.forge.entity.WorkoutSession;
import com.forge.entity.enums.ExerciseCategory;
import com.forge.repository.ExerciseLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlateauDetectionService Tests")
class PlateauDetectionServiceTest {

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @Mock
    private com.forge.repository.WorkoutSessionRepository workoutSessionRepository;

    @InjectMocks
    private PlateauDetectionService plateauDetectionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test User").email("test@example.com").build();
    }

    @Test
    @DisplayName("Should not detect plateau when exercise data is empty")
    void detectPlateaus_ShouldReturnEmpty_WhenNoExercises() {
        when(exerciseLogRepository.findDistinctExerciseNamesByUserId(anyLong()))
                .thenReturn(List.of());

        var alerts = plateauDetectionService.detectPlateaus(1L);

        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should not detect plateau with insufficient exercise data")
    void detectPlateaus_ShouldReturnEmpty_WhenInsufficientData() {
        when(exerciseLogRepository.findDistinctExerciseNamesByUserId(anyLong()))
                .thenReturn(List.of("Bench Press"));
        when(exerciseLogRepository.findByUserIdAndExerciseNameOrderByDate(anyLong(), anyString()))
                .thenReturn(List.of(
                        ExerciseLog.builder().exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build(),
                        ExerciseLog.builder().exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build()
                ));

        var alerts = plateauDetectionService.detectPlateaus(1L);

        assertThat(alerts).noneMatch(a -> a.getType().equals("STRENGTH"));
    }

    @Test
    @DisplayName("Should detect strength plateau when volume is stagnant")
    void detectPlateaus_ShouldDetectStrengthPlateau_WhenVolumeStagnant() {
        WorkoutSession ws = WorkoutSession.builder().id(1L).user(testUser).date(LocalDate.now()).build();

        List<ExerciseLog> stagnantLogs = List.of(
                ExerciseLog.builder().workoutSession(ws).exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build(),
                ExerciseLog.builder().workoutSession(ws).exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build(),
                ExerciseLog.builder().workoutSession(ws).exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build(),
                ExerciseLog.builder().workoutSession(ws).exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build(),
                ExerciseLog.builder().workoutSession(ws).exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build(),
                ExerciseLog.builder().workoutSession(ws).exerciseName("Bench Press").category(ExerciseCategory.STRENGTH).weightKg(60.0).reps(10).sets(3).build()
        );

        when(exerciseLogRepository.findDistinctExerciseNamesByUserId(anyLong()))
                .thenReturn(List.of("Bench Press"));
        when(exerciseLogRepository.findByUserIdAndExerciseNameOrderByDate(anyLong(), anyString()))
                .thenReturn(stagnantLogs);

        var alerts = plateauDetectionService.detectPlateaus(1L);

        assertThat(alerts).anyMatch(a -> a.getType().equals("STRENGTH"));
    }
}
