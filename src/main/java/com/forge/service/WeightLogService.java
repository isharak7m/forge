package com.forge.service;

import com.forge.entity.User;
import com.forge.entity.weight.WeightLog;
import com.forge.exception.ResourceNotFoundException;
import com.forge.repository.UserRepository;
import com.forge.repository.WeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final WeightLogRepository weightLogRepository;
    private final UserRepository userRepository;

    public WeightLog recordWeight(Long userId, LocalDate date, Double weightKg) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WeightLog log = WeightLog.builder()
                .user(user)
                .date(date != null ? date : LocalDate.now())
                .weightKg(weightKg)
                .build();

        WeightLog saved = weightLogRepository.save(log);

        user.setCurrentWeightKg(weightKg);
        userRepository.save(user);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<WeightLog> getHistory(Long userId, int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        return weightLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
    }

    @Transactional(readOnly = true)
    public List<WeightLog> getHistory(Long userId, LocalDate from, LocalDate to) {
        return weightLogRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
    }

    @Transactional(readOnly = true)
    public Double getCurrentWeight(Long userId) {
        return weightLogRepository.findTopByUserIdOrderByDateDesc(userId)
                .map(WeightLog::getWeightKg)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Double getStartWeight(Long userId) {
        return weightLogRepository.findTopByUserIdOrderByDateAsc(userId)
                .map(WeightLog::getWeightKg)
                .orElse(null);
    }
}
