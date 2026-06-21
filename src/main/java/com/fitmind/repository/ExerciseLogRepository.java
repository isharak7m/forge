package com.fitmind.repository;

import com.fitmind.entity.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    @Query("SELECT el FROM ExerciseLog el JOIN el.workoutSession ws WHERE ws.user.id = :userId ORDER BY ws.date DESC")
    List<ExerciseLog> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT el FROM ExerciseLog el JOIN el.workoutSession ws WHERE ws.user.id = :userId AND ws.date BETWEEN :from AND :to ORDER BY ws.date DESC")
    List<ExerciseLog> findByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT el FROM ExerciseLog el JOIN el.workoutSession ws WHERE ws.user.id = :userId AND LOWER(el.exerciseName) = LOWER(:exerciseName) ORDER BY ws.date ASC")
    List<ExerciseLog> findByUserIdAndExerciseNameOrderByDate(@Param("userId") Long userId, @Param("exerciseName") String exerciseName);

    @Query("SELECT DISTINCT el.exerciseName FROM ExerciseLog el JOIN el.workoutSession ws WHERE ws.user.id = :userId")
    List<String> findDistinctExerciseNamesByUserId(@Param("userId") Long userId);

    @Query("SELECT el FROM ExerciseLog el JOIN el.workoutSession ws WHERE ws.user.id = :userId AND LOWER(el.exerciseName) = LOWER(:exerciseName) ORDER BY (el.weightKg * el.reps * el.sets) DESC")
    List<ExerciseLog> findPersonalRecordsByExercise(@Param("userId") Long userId, @Param("exerciseName") String exerciseName);

    @Query("SELECT el.category, COUNT(el) FROM ExerciseLog el JOIN el.workoutSession ws WHERE ws.user.id = :userId AND ws.date BETWEEN :from AND :to GROUP BY el.category")
    List<Object[]> countExercisesByCategory(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
