package com.forge.repository;

import com.forge.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate from, LocalDate to);

    List<WorkoutSession> findByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.date BETWEEN :from AND :to")
    Long countByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT DISTINCT ws.date FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.date BETWEEN :from AND :to ORDER BY ws.date")
    List<LocalDate> findDistinctDatesByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
