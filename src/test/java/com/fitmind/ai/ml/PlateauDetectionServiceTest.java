package com.fitmind.ai.ml;

import com.fitmind.entity.BodyMetric;
import com.fitmind.entity.User;
import com.fitmind.repository.BodyMetricRepository;
import com.fitmind.repository.ExerciseLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlateauDetectionService Tests")
class PlateauDetectionServiceTest {

    @Mock
    private BodyMetricRepository bodyMetricRepository;

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @Mock
    private com.fitmind.repository.WorkoutSessionRepository workoutSessionRepository;

    @InjectMocks
    private PlateauDetectionService plateauDetectionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test User").email("test@example.com").build();
    }

    @Test
    @DisplayName("Should detect weight plateau when weight variation is minimal")
    void detectPlateaus_ShouldDetectWeightPlateau_WhenVariationUnder0_5kg() {
        List<BodyMetric> metrics = new ArrayList<>();
        double[] stagnantWeights = {78.0, 78.1, 78.0, 78.2, 78.1, 78.0, 78.1};
        LocalDate base = LocalDate.now().minusDays(20);

        for (int i = 0; i < stagnantWeights.length; i++) {
            metrics.add(BodyMetric.builder()
                    .user(testUser)
                    .recordedDate(base.plusDays(i * 3))
                    .weightKg(stagnantWeights[i])
                    .build());
        }

        when(bodyMetricRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                anyLong(), any(), any())).thenReturn(metrics);
        when(exerciseLogRepository.findDistinctExerciseNamesByUserId(anyLong()))
                .thenReturn(List.of());

        var alerts = plateauDetectionService.detectPlateaus(1L);

        assertThat(alerts).isNotEmpty();
        assertThat(alerts).anyMatch(a -> a.getType().equals("WEIGHT"));
    }

    @Test
    @DisplayName("Should NOT detect plateau when weight is actively changing")
    void detectPlateaus_ShouldNotDetectPlateau_WhenWeightChanging() {
        List<BodyMetric> metrics = new ArrayList<>();
        double[] changingWeights = {80.0, 79.5, 79.0, 78.5, 78.0, 77.5, 77.0};
        LocalDate base = LocalDate.now().minusDays(20);

        for (int i = 0; i < changingWeights.length; i++) {
            metrics.add(BodyMetric.builder()
                    .user(testUser)
                    .recordedDate(base.plusDays(i * 3))
                    .weightKg(changingWeights[i])
                    .build());
        }

        when(bodyMetricRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                anyLong(), any(), any())).thenReturn(metrics);
        when(exerciseLogRepository.findDistinctExerciseNamesByUserId(anyLong()))
                .thenReturn(List.of());

        var alerts = plateauDetectionService.detectPlateaus(1L);

        assertThat(alerts).noneMatch(a -> a.getType().equals("WEIGHT"));
    }

    @Test
    @DisplayName("Should return empty list when insufficient data")
    void detectPlateaus_ShouldReturnEmpty_WhenInsufficientData() {
        when(bodyMetricRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(
                anyLong(), any(), any())).thenReturn(List.of());
        when(exerciseLogRepository.findDistinctExerciseNamesByUserId(anyLong()))
                .thenReturn(List.of());

        var alerts = plateauDetectionService.detectPlateaus(1L);

        assertThat(alerts).noneMatch(a -> a.getType().equals("WEIGHT"));
    }
}
