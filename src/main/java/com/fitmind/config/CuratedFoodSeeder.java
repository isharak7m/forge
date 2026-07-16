package com.fitmind.config;

import com.fitmind.entity.FoodItem;
import com.fitmind.repository.FoodItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CuratedFoodSeeder {

    private final FoodItemRepository foodItemRepository;

    @PostConstruct
    public void seedIfNeeded() {
        try {
            long count = foodItemRepository.count();
            if (count >= 400 && count <= 600) {
                log.info("Curated food database already populated ({} items), skipping seed.", count);
                return;
            }
            if (count > 0) {
                log.info("Clearing {} existing food items before re-seed", count);
                foodItemRepository.deleteAllInBatch();
            }

            String projectDir = System.getProperty("app.project.dir",
                    System.getProperty("user.dir", "."));
            String csvPath = Paths.get(projectDir, "nutrition_dataset_final.csv").toString();
            log.info("Loading curated food database from: {}", csvPath);

            List<FoodItem> items = parseCsv(csvPath);
            log.info("Parsed {} food items from CSV", items.size());

            int batchSize = 200;
            for (int i = 0; i < items.size(); i += batchSize) {
                int end = Math.min(i + batchSize, items.size());
                foodItemRepository.saveAll(items.subList(i, end));
                log.info("Seeded {}/{}", end, items.size());
            }
            log.info("Curated food database seeded: {} items", items.size());
        } catch (Exception e) {
            log.error("Failed to seed curated food database: {}", e.getMessage(), e);
        }
    }

    private List<FoodItem> parseCsv(String path) throws IOException {
        List<FoodItem> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String header = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = parseCsvLine(line);
                if (cols.length < 6) continue;
                items.add(mapToFoodItem(cols));
            }
        }
        return items;
    }

    private FoodItem mapToFoodItem(String[] c) {
        FoodItem.FoodItemBuilder b = FoodItem.builder()
                .name(get(c, 0))
                .category(get(c, 1))
                .caloriesPer100g(parseDbl(c, 2))
                .proteinPer100g(parseDbl(c, 3))
                .carbsPer100g(parseDbl(c, 4))
                .fatPer100g(parseDbl(c, 5))
                .fiberPer100g(parseDbl(c, 6))
                .vitaminAPer100g(parseDbl(c, 7))
                .vitaminCPer100g(parseDbl(c, 8))
                .vitaminDPer100g(parseDbl(c, 9))
                .vitaminEPer100g(parseDbl(c, 10))
                .vitaminKPer100g(parseDbl(c, 11))
                .vitaminB1Per100g(parseDbl(c, 12))
                .vitaminB2Per100g(parseDbl(c, 13))
                .vitaminB3Per100g(parseDbl(c, 14))
                .vitaminB5Per100g(parseDbl(c, 15))
                .vitaminB6Per100g(parseDbl(c, 16))
                .vitaminB7Per100g(parseDbl(c, 17))
                .vitaminB9Per100g(parseDbl(c, 18))
                .vitaminB12Per100g(parseDbl(c, 19))
                .calciumPer100g(parseDbl(c, 20))
                .ironPer100g(parseDbl(c, 21))
                .magnesiumPer100g(parseDbl(c, 22))
                .potassiumPer100g(parseDbl(c, 23))
                .sodiumPer100g(parseDbl(c, 24))
                .zincPer100g(parseDbl(c, 25))
                .copperPer100g(parseDbl(c, 26))
                .manganesePer100g(parseDbl(c, 27))
                .seleniumPer100g(parseDbl(c, 28))
                .phosphorusPer100g(parseDbl(c, 29))
                .iodinePer100g(parseDbl(c, 30))
                .chromiumPer100g(parseDbl(c, 31));
        return b.build();
    }

    private String get(String[] arr, int idx) {
        return (idx >= 0 && idx < arr.length) ? arr[idx].replace("\"", "").trim() : "";
    }

    private Double parseDbl(String[] arr, int idx) {
        String val = get(arr, idx);
        if (val.isEmpty()) return 0.0;
        try { return Math.round(Double.parseDouble(val) * 1000.0) / 1000.0; }
        catch (NumberFormatException e) { return 0.0; }
    }

    private String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()) return new String[0];
        if (!line.contains("\"")) return line.split(",");
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }
}