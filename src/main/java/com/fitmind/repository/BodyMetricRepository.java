package com.fitmind.repository;

import com.fitmind.entity.BodyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BodyMetricRepository extends JpaRepository<BodyMetric, Long> {

    List<BodyMetric> findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(Long userId, LocalDate from, LocalDate to);

    List<BodyMetric> findByUserIdOrderByRecordedDateDesc(Long userId);

    Optional<BodyMetric> findTopByUserIdOrderByRecordedDateDesc(Long userId);

    @Query("SELECT bm FROM BodyMetric bm WHERE bm.user.id = :userId AND bm.recordedDate >= :from ORDER BY bm.recordedDate ASC")
    List<BodyMetric> findRecentByUserId(@Param("userId") Long userId, @Param("from") LocalDate from);

    @Query("SELECT AVG(bm.sleepHours) FROM BodyMetric bm WHERE bm.user.id = :userId AND bm.recordedDate BETWEEN :from AND :to")
    Double avgSleepHoursByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT AVG(bm.weightKg) FROM BodyMetric bm WHERE bm.user.id = :userId AND bm.recordedDate BETWEEN :from AND :to")
    Double avgWeightByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
