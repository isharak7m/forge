package com.fitmind.repository;

import com.fitmind.entity.SleepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleepLogRepository extends JpaRepository<SleepLog, Long> {

    Optional<SleepLog> findTopByUserIdAndDateOrderByLoggedAtDesc(Long userId, LocalDate date);

    @Query("SELECT AVG(s.durationHours) FROM SleepLog s WHERE s.user.id = :userId AND s.date BETWEEN :from AND :to")
    Double avgSleepHoursByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT AVG(s.qualityScore) FROM SleepLog s WHERE s.user.id = :userId AND s.date BETWEEN :from AND :to")
    Double avgQualityByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    List<SleepLog> findByUserId(Long userId);

    List<SleepLog> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);
}
