package com.fitmind.service;

import com.fitmind.dto.metrics.BodyMetricRequest;
import com.fitmind.dto.metrics.BodyMetricResponse;
import com.fitmind.dto.metrics.TrendPoint;
import com.fitmind.entity.BodyMetric;
import com.fitmind.entity.User;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.BodyMetricRepository;
import com.fitmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BodyMetricService {

    private final BodyMetricRepository metricRepository;
    private final UserRepository userRepository;

    public BodyMetricResponse recordMetric(Long userId, BodyMetricRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BodyMetric metric = BodyMetric.builder()
                .user(user)
                .recordedDate(request.getRecordedDate() != null ? request.getRecordedDate() : LocalDate.now())
                .weightKg(request.getWeightKg())
                .waistCm(request.getWaistCm())
                .chestCm(request.getChestCm())
                .armsCm(request.getArmsCm())
                .thighsCm(request.getThighsCm())
                .bodyFatPercentage(request.getBodyFatPercentage())
                .sleepHours(request.getSleepHours())
                .waterLiters(request.getWaterLiters())
                .recoveryScore(request.getRecoveryScore())
                .notes(request.getNotes())
                .build();

        BodyMetric saved = metricRepository.save(metric);
        
        // Update user's current weight if provided
        if (request.getWeightKg() != null) {
            user.setCurrentWeightKg(request.getWeightKg());
            userRepository.save(user);
        }
        
        return mapToResponse(saved);
    }

    public List<BodyMetricResponse> getMetrics(Long userId, LocalDate from, LocalDate to) {
        return metricRepository.findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(userId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TrendPoint> getWeightTrend(Long userId) {
        List<BodyMetric> metrics = metricRepository.findByUserIdOrderByRecordedDateDesc(userId);
        return metrics.stream()
                .filter(m -> m.getWeightKg() != null)
                .map(m -> TrendPoint.builder()
                        .date(m.getRecordedDate())
                        .value(m.getWeightKg())
                        .label("Weight (kg)")
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, List<TrendPoint>> getMeasurementTrends(Long userId) {
        List<BodyMetric> metrics = metricRepository.findByUserIdOrderByRecordedDateDesc(userId);
        Map<String, List<TrendPoint>> trends = new HashMap<>();
        
        trends.put("waist", extractTrend(metrics, "Waist (cm)", m -> m.getWaistCm()));
        trends.put("chest", extractTrend(metrics, "Chest (cm)", m -> m.getChestCm()));
        trends.put("arms", extractTrend(metrics, "Arms (cm)", m -> m.getArmsCm()));
        trends.put("thighs", extractTrend(metrics, "Thighs (cm)", m -> m.getThighsCm()));
        
        return trends;
    }

    public List<TrendPoint> getSleepTrend(Long userId) {
        List<BodyMetric> metrics = metricRepository.findByUserIdOrderByRecordedDateDesc(userId);
        return extractTrend(metrics, "Sleep (hours)", BodyMetric::getSleepHours);
    }

    public List<TrendPoint> getRecoveryTrend(Long userId) {
        List<BodyMetric> metrics = metricRepository.findByUserIdOrderByRecordedDateDesc(userId);
        return extractTrend(metrics, "Recovery Score", m -> m.getRecoveryScore() != null ? m.getRecoveryScore().doubleValue() : null);
    }

    private List<TrendPoint> extractTrend(List<BodyMetric> metrics, String label, java.util.function.Function<BodyMetric, Double> extractor) {
        return metrics.stream()
                .filter(m -> extractor.apply(m) != null)
                .map(m -> TrendPoint.builder()
                        .date(m.getRecordedDate())
                        .value(extractor.apply(m))
                        .label(label)
                        .build())
                .collect(Collectors.toList());
    }

    private BodyMetricResponse mapToResponse(BodyMetric metric) {
        return BodyMetricResponse.builder()
                .id(metric.getId())
                .recordedDate(metric.getRecordedDate())
                .weightKg(metric.getWeightKg())
                .waistCm(metric.getWaistCm())
                .chestCm(metric.getChestCm())
                .armsCm(metric.getArmsCm())
                .thighsCm(metric.getThighsCm())
                .bodyFatPercentage(metric.getBodyFatPercentage())
                .sleepHours(metric.getSleepHours())
                .waterLiters(metric.getWaterLiters())
                .recoveryScore(metric.getRecoveryScore())
                .notes(metric.getNotes())
                .build();
    }
}
