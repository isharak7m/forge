package com.forge.repository;

import com.forge.entity.FoodLog;
import com.forge.entity.enums.MealCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByUserIdAndDateOrderByLoggedAtAsc(Long userId, LocalDate date);

    List<FoodLog> findByUserIdAndDateAndMealCategory(Long userId, LocalDate date, MealCategory category);

    List<FoodLog> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT SUM(f.calories) FROM FoodLog f WHERE f.user.id = :userId AND f.date = :date")
    Double sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT SUM(f.calories) FROM FoodLog f WHERE f.user.id = :userId AND f.date BETWEEN :from AND :to")
    Double sumCaloriesByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT SUM(f.proteinG) FROM FoodLog f WHERE f.user.id = :userId AND f.date BETWEEN :from AND :to")
    Double sumProteinByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT SUM(f.carbsG) FROM FoodLog f WHERE f.user.id = :userId AND f.date BETWEEN :from AND :to")
    Double sumCarbsByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT SUM(f.fatG) FROM FoodLog f WHERE f.user.id = :userId AND f.date BETWEEN :from AND :to")
    Double sumFatByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(DISTINCT f.date) FROM FoodLog f WHERE f.user.id = :userId AND f.date BETWEEN :from AND :to")
    Long countDistinctDatesLogged(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT f.date, SUM(f.calories) FROM FoodLog f WHERE f.user.id = :userId AND f.date BETWEEN :from AND :to GROUP BY f.date ORDER BY f.date")
    List<Object[]> findDailyCaloriesByDateRange(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(f) FROM FoodLog f WHERE f.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FoodLog f WHERE f.user.id = :userId ORDER BY f.date ASC LIMIT 1")
    java.util.Optional<FoodLog> findFirstByUserIdOrderByDateAsc(@Param("userId") Long userId);

    @Query("SELECT MAX(f.date) FROM FoodLog f WHERE f.user.id = :userId")
    java.util.Optional<LocalDate> findMaxDateByUserId(@Param("userId") Long userId);
}
