package com.forge.dto.metrics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TrendPoint {
    private LocalDate date;
    private Double value;
    private String label;
}
