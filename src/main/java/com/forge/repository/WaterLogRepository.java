package com.forge.repository;

import com.forge.entity.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {

    Optional<WaterLog> findTopByUserIdAndDateOrderByLoggedAtDesc(Long userId, LocalDate date);

    @Query("SELECT AVG(w.amountMl) FROM WaterLog w WHERE w.user.id = :userId AND w.date BETWEEN :from AND :to")
    Double avgWaterByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    List<WaterLog> findByUserId(Long userId);

    List<WaterLog> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);
}
