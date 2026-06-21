package com.fitmind.service;

import com.fitmind.dto.workout.*;
import com.fitmind.entity.ExerciseLog;
import com.fitmind.entity.User;
import com.fitmind.entity.WorkoutSession;
import com.fitmind.exception.ResourceNotFoundException;
import com.fitmind.repository.ExerciseLogRepository;
import com.fitmind.repository.UserRepository;
import com.fitmind.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutSessionRepository sessionRepository;
    private final ExerciseLogRepository exerciseRepository;
    private final UserRepository userRepository;

    public WorkoutSessionResponse createSession(Long userId, WorkoutSessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        WorkoutSession session = WorkoutSession.builder()
                .user(user)
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .name(request.getName())
                .notes(request.getNotes())
                .durationMinutes(request.getDurationMinutes())
                .exercises(new ArrayList<>())
                .build();

        WorkoutSession saved = sessionRepository.save(session);
        return mapToResponse(saved);
    }

    public WorkoutSessionResponse addExercise(Long userId, Long sessionId, ExerciseLogRequest request) {
        WorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        ExerciseLog exercise = ExerciseLog.builder()
                .workoutSession(session)
                .exerciseName(request.getExerciseName())
                .category(request.getCategory())
                .sets(request.getSets())
                .reps(request.getReps())
                .weightKg(request.getWeightKg())
                .rpe(request.getRpe())
                .notes(request.getNotes())
                .build();

        exerciseRepository.save(exercise);
        
        // Refresh session
        session = sessionRepository.findById(sessionId).orElseThrow();
        return mapToResponse(session);
    }

    public List<WorkoutSessionResponse> getSessions(Long userId, LocalDate from, LocalDate to) {
        return sessionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Double> getVolumeAnalytics(Long userId, LocalDate from, LocalDate to) {
        List<WorkoutSession> sessions = sessionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, from, to);
        Map<String, Double> volumeByDate = new LinkedHashMap<>();
        
        for (WorkoutSession session : sessions) {
            double volume = calculateTotalVolume(session.getExercises());
            volumeByDate.put(session.getDate().toString(), volumeByDate.getOrDefault(session.getDate().toString(), 0.0) + volume);
        }
        
        return volumeByDate;
    }

    public List<PersonalRecord> getPersonalRecords(Long userId) {
        List<String> exercises = exerciseRepository.findDistinctExerciseNamesByUserId(userId);
        List<PersonalRecord> prs = new ArrayList<>();
        
        for (String exerciseName : exercises) {
            List<ExerciseLog> logs = exerciseRepository.findPersonalRecordsByExercise(userId, exerciseName);
            if (!logs.isEmpty()) {
                ExerciseLog best = logs.get(0); // Query already orders by volume DESC
                double volume = calculateExerciseVolume(best);
                double epley1rm = best.getWeightKg() * (1.0 + (best.getReps() / 30.0));
                
                prs.add(PersonalRecord.builder()
                        .exerciseName(best.getExerciseName())
                        .weightKg(best.getWeightKg())
                        .reps(best.getReps())
                        .sets(best.getSets())
                        .volume(volume)
                        .achievedDate(best.getWorkoutSession().getDate())
                        .estimatedOneRepMax(epley1rm)
                        .build());
            }
        }
        return prs;
    }

    public Map<String, Long> getMuscleGroupFrequency(Long userId, LocalDate from, LocalDate to) {
        List<Object[]> results = exerciseRepository.countExercisesByCategory(userId, from, to);
        Map<String, Long> frequency = new HashMap<>();
        for (Object[] result : results) {
            if (result[0] != null) {
                frequency.put(result[0].toString(), (Long) result[1]);
            }
        }
        return frequency;
    }

    public List<ProgressionPoint> getExerciseProgression(Long userId, String exerciseName) {
        List<ExerciseLog> logs = exerciseRepository.findByUserIdAndExerciseNameOrderByDate(userId, exerciseName);
        return logs.stream().map(log -> ProgressionPoint.builder()
                .date(log.getWorkoutSession().getDate())
                .volume(calculateExerciseVolume(log))
                .maxWeight(log.getWeightKg())
                .totalReps(log.getReps() * log.getSets())
                .build()
        ).collect(Collectors.toList());
    }

    private WorkoutSessionResponse mapToResponse(WorkoutSession session) {
        List<ExerciseLogResponse> exerciseResponses = session.getExercises() != null ? 
                session.getExercises().stream().map(this::mapToResponse).collect(Collectors.toList()) : 
                new ArrayList<>();

        double totalVolume = calculateTotalVolume(session.getExercises());

        return WorkoutSessionResponse.builder()
                .id(session.getId())
                .date(session.getDate())
                .name(session.getName())
                .notes(session.getNotes())
                .durationMinutes(session.getDurationMinutes())
                .exercises(exerciseResponses)
                .totalVolume(totalVolume)
                .createdAt(session.getCreatedAt())
                .build();
    }

    private ExerciseLogResponse mapToResponse(ExerciseLog log) {
        return ExerciseLogResponse.builder()
                .id(log.getId())
                .exerciseName(log.getExerciseName())
                .category(log.getCategory())
                .sets(log.getSets())
                .reps(log.getReps())
                .weightKg(log.getWeightKg())
                .rpe(log.getRpe())
                .notes(log.getNotes())
                .volume(calculateExerciseVolume(log))
                .build();
    }

    private double calculateExerciseVolume(ExerciseLog log) {
        if (log.getWeightKg() == null || log.getReps() == null || log.getSets() == null) return 0.0;
        return log.getWeightKg() * log.getReps() * log.getSets();
    }

    private double calculateTotalVolume(List<ExerciseLog> exercises) {
        if (exercises == null || exercises.isEmpty()) return 0.0;
        return exercises.stream().mapToDouble(this::calculateExerciseVolume).sum();
    }
}
