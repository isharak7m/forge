package com.fitmind.service;

import com.fitmind.entity.User;
import com.fitmind.entity.WaterLog;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.UserRepository;
import com.fitmind.repository.WaterLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaterLogService {

    private final WaterLogRepository waterLogRepository;
    private final UserRepository userRepository;

    public WaterLog recordWater(Long userId, LocalDate date, Double amountMl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WaterLog log = WaterLog.builder()
                .user(user)
                .date(date != null ? date : LocalDate.now())
                .amountMl(amountMl)
                .build();

        return waterLogRepository.save(log);
    }

    public Double getDailyWater(Long userId, LocalDate date) {
        return waterLogRepository.findTopByUserIdAndDateOrderByLoggedAtDesc(userId, date)
                .map(WaterLog::getAmountMl)
                .orElse(0.0);
    }

    public Double getAverageWater(Long userId, LocalDate from, LocalDate to) {
        Double avg = waterLogRepository.avgWaterByUserIdAndDateBetween(userId, from, to);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public List<WaterLog> getHistory(Long userId, LocalDate from, LocalDate to) {
        return waterLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
    }
}
