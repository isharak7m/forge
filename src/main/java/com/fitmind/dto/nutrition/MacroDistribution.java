package com.fitmind.dto.nutrition;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MacroDistribution {
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double proteinPct;
    private double carbsPct;
    private double fatPct;
    private double proteinCalories;
    private double carbCalories;
    private double fatCalories;
}
