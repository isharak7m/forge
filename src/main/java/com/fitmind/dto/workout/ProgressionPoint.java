package com.fitmind.dto.workout;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProgressionPoint {
    private LocalDate date;
    private Double volume;
    private Double maxWeight;
    private Integer totalReps;
}
