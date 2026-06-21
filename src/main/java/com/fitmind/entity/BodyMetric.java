package com.fitmind.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "body_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate recordedDate;

    private Double weightKg;
    private Double waistCm;
    private Double chestCm;
    private Double armsCm;
    private Double thighsCm;
    private Double bodyFatPercentage;
    private Double sleepHours;
    private Double waterLiters;
    private Integer recoveryScore; // subjective 1-10
    private String notes;
}
