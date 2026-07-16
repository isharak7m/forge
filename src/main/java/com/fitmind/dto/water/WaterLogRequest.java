package com.fitmind.dto.water;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WaterLogRequest {
    private LocalDate date;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be positive")
    @Max(value = 10000, message = "Amount seems too high")
    private Double amountMl;
}
