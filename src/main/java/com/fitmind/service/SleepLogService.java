package com.fitmind.service;

import com.fitmind.entity.SleepLog;
import com.fitmind.entity.User;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.SleepLogRepository;
import com.fitmind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepLogService {

    private final SleepLogRepository sleepLogRepository;
    private final UserRepository userRepository;

    public SleepLog recordSleep(Long userId, LocalDate date, Double durationHours, Integer qualityScore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SleepLog log = SleepLog.builder()
                .user(user)
                .date(date != null ? date : LocalDate.now())
                .durationHours(durationHours)
                .qualityScore(qualityScore)
                .build();

        return sleepLogRepository.save(log);
    }

    public Double getDailySleepHours(Long userId, LocalDate date) {
        return sleepLogRepository.findTopByUserIdAndDateOrderByLoggedAtDesc(userId, date)
                .map(SleepLog::getDurationHours)
                .orElse(0.0);
    }

    public Double getAverageSleepHours(Long userId, LocalDate from, LocalDate to) {
        Double avg = sleepLogRepository.avgSleepHoursByUserIdAndDateBetween(userId, from, to);
        return avg != null ? avg : 0.0;
    }

    public Double getAverageQuality(Long userId, LocalDate from, LocalDate to) {
        Double avg = sleepLogRepository.avgQualityByUserIdAndDateBetween(userId, from, to);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public List<SleepLog> getHistory(Long userId, LocalDate from, LocalDate to) {
        return sleepLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
    }
}
