package com.forge.entity;

import com.forge.entity.enums.CardioZone;
import com.forge.entity.enums.ExerciseCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercise_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @Column(nullable = false)
    private String exerciseName;

    @Enumerated(EnumType.STRING)
    private ExerciseCategory category;

    private Integer sets;
    private Integer reps;
    private Double weightKg;
    private Integer rpe;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    private CardioZone zone;

    private String notes;
}
