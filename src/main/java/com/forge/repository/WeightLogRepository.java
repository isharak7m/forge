package com.forge.repository;

import com.forge.entity.weight.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {

    Optional<WeightLog> findTopByUserIdAndDateOrderByCreatedAtDesc(Long userId, LocalDate date);

    @Query("SELECT AVG(w.weightKg) FROM WeightLog w WHERE w.user.id = :userId AND w.date BETWEEN :from AND :to")
    Double avgWeightByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    List<WeightLog> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    Optional<WeightLog> findTopByUserIdOrderByDateDesc(Long userId);

    Optional<WeightLog> findTopByUserIdOrderByDateAsc(Long userId);

    long countByUserId(Long userId);

    List<WeightLog> findByUserIdOrderByDateAsc(Long userId);
}
