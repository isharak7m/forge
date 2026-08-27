package com.forge.service;

import com.forge.entity.User;
import com.forge.entity.WaterLog;
import com.forge.exception.ResourceNotFoundException;
import com.forge.repository.UserRepository;
import com.forge.repository.WaterLogRepository;
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
