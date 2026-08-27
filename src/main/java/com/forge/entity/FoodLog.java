package com.forge.entity;

import com.forge.entity.enums.MealCategory;
import com.forge.entity.enums.ServingUnit;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealCategory mealCategory;

    @Column(nullable = false)
    private String foodName;

    private Double servingSize;

    @Enumerated(EnumType.STRING)
    private ServingUnit unit;

    private Double calories;
    private Double proteinG;
    private Double carbsG;
    private Double fatG;
    private Double fiberG;

    private Double vitaminA;
    private Double vitaminC;
    private Double vitaminD;
    private Double vitaminE;
    private Double vitaminK;
    private Double vitaminB1;
    private Double vitaminB2;
    private Double vitaminB3;
    private Double vitaminB5;
    private Double vitaminB6;
    private Double vitaminB7;
    private Double vitaminB9;
    private Double vitaminB12;

    private Double calcium;
    private Double iron;
    private Double magnesium;
    private Double potassium;
    private Double sodium;
    private Double zinc;
    private Double copper;
    private Double manganese;
    private Double selenium;
    private Double phosphorus;
    private Double iodine;
    private Double chromium;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime loggedAt;
}
