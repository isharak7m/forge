package com.fitmind.config;

import com.fitmind.entity.FoodItem;
import com.fitmind.repository.FoodItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
            if (count >= 100) {
                log.info("Food database already populated ({} items), skipping seed.", count);
                return;
            }
            if (count > 0) {
                foodItemRepository.deleteAllInBatch();
            }

            List<FoodItem> items = buildFoodList();
            int batchSize = 200;
            for (int i = 0; i < items.size(); i += batchSize) {
                int end = Math.min(i + batchSize, items.size());
                foodItemRepository.saveAll(items.subList(i, end));
            }
            log.info("Food database seeded: {} items", items.size());
        } catch (Exception e) {
            log.error("Failed to seed food database: {}", e.getMessage(), e);
        }
    }

    private List<FoodItem> buildFoodList() {
        List<FoodItem> items = new ArrayList<>();

        // ── Meat & Poultry ──
        items.add( FoodItem.builder().name("Chicken Breast").category("Meat & Poultry").caloriesPer100g(165.0).proteinPer100g(31.0).carbsPer100g(0.0).fatPer100g(3.6).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Chicken Thigh").category("Meat & Poultry").caloriesPer100g(209.0).proteinPer100g(26.0).carbsPer100g(0.0).fatPer100g(11.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Ground Beef 80/20").category("Meat & Poultry").caloriesPer100g(254.0).proteinPer100g(17.0).carbsPer100g(0.0).fatPer100g(20.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Ground Beef 93/7").category("Meat & Poultry").caloriesPer100g(177.0).proteinPer100g(20.0).carbsPer100g(0.0).fatPer100g(7.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Beef Steak (Sirloin)").category("Meat & Poultry").caloriesPer100g(206.0).proteinPer100g(26.0).carbsPer100g(0.0).fatPer100g(11.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Beef Steak (Ribeye)").category("Meat & Poultry").caloriesPer100g(271.0).proteinPer100g(24.0).carbsPer100g(0.0).fatPer100g(19.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Pork Chop").category("Meat & Poultry").caloriesPer100g(231.0).proteinPer100g(25.0).carbsPer100g(0.0).fatPer100g(14.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Pork Tenderloin").category("Meat & Poultry").caloriesPer100g(143.0).proteinPer100g(26.0).carbsPer100g(0.0).fatPer100g(3.5).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Bacon").category("Meat & Poultry").caloriesPer100g(541.0).proteinPer100g(37.0).carbsPer100g(1.4).fatPer100g(42.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Turkey Breast").category("Meat & Poultry").caloriesPer100g(135.0).proteinPer100g(29.0).carbsPer100g(0.0).fatPer100g(1.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Lamb Chop").category("Meat & Poultry").caloriesPer100g(258.0).proteinPer100g(25.0).carbsPer100g(0.0).fatPer100g(17.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Duck Breast").category("Meat & Poultry").caloriesPer100g(200.0).proteinPer100g(23.0).carbsPer100g(0.0).fatPer100g(11.0).fiberPer100g(0.0).build());

        // ── Fish & Seafood ──
        items.add( FoodItem.builder().name("Salmon (Atlantic)").category("Fish & Seafood").caloriesPer100g(208.0).proteinPer100g(20.0).carbsPer100g(0.0).fatPer100g(13.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Tuna (Canned in Water)").category("Fish & Seafood").caloriesPer100g(116.0).proteinPer100g(26.0).carbsPer100g(0.0).fatPer100g(0.8).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Shrimp").category("Fish & Seafood").caloriesPer100g(99.0).proteinPer100g(24.0).carbsPer100g(0.0).fatPer100g(0.3).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Cod").category("Fish & Seafood").caloriesPer100g(82.0).proteinPer100g(18.0).carbsPer100g(0.0).fatPer100g(0.7).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Tilapia").category("Fish & Seafood").caloriesPer100g(96.0).proteinPer100g(20.0).carbsPer100g(0.0).fatPer100g(1.7).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Mackerel").category("Fish & Seafood").caloriesPer100g(262.0).proteinPer100g(19.0).carbsPer100g(0.0).fatPer100g(20.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Sardines (Canned)").category("Fish & Seafood").caloriesPer100g(208.0).proteinPer100g(25.0).carbsPer100g(0.0).fatPer100g(11.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Tuna Steak").category("Fish & Seafood").caloriesPer100g(184.0).proteinPer100g(28.0).carbsPer100g(0.0).fatPer100g(6.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Sea Bass").category("Fish & Seafood").caloriesPer100g(124.0).proteinPer100g(21.0).carbsPer100g(0.0).fatPer100g(3.7).fiberPer100g(0.0).build());

        // ── Dairy & Eggs ──
        items.add( FoodItem.builder().name("Whole Egg").category("Dairy & Eggs").caloriesPer100g(155.0).proteinPer100g(13.0).carbsPer100g(1.1).fatPer100g(11.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Egg White").category("Dairy & Eggs").caloriesPer100g(52.0).proteinPer100g(11.0).carbsPer100g(0.7).fatPer100g(0.2).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Whole Milk").category("Dairy & Eggs").caloriesPer100g(61.0).proteinPer100g(3.2).carbsPer100g(4.8).fatPer100g(3.3).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Skim Milk").category("Dairy & Eggs").caloriesPer100g(35.0).proteinPer100g(3.4).carbsPer100g(5.0).fatPer100g(0.1).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Greek Yogurt (Plain)").category("Dairy & Eggs").caloriesPer100g(97.0).proteinPer100g(9.0).carbsPer100g(3.6).fatPer100g(5.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Cheddar Cheese").category("Dairy & Eggs").caloriesPer100g(403.0).proteinPer100g(25.0).carbsPer100g(1.3).fatPer100g(33.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Mozzarella Cheese").category("Dairy & Eggs").caloriesPer100g(280.0).proteinPer100g(28.0).carbsPer100g(3.1).fatPer100g(17.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Cottage Cheese").category("Dairy & Eggs").caloriesPer100g(98.0).proteinPer100g(11.0).carbsPer100g(3.4).fatPer100g(4.3).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Butter").category("Dairy & Eggs").caloriesPer100g(717.0).proteinPer100g(0.9).carbsPer100g(0.1).fatPer100g(81.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Heavy Cream").category("Dairy & Eggs").caloriesPer100g(345.0).proteinPer100g(2.8).carbsPer100g(2.8).fatPer100g(37.0).fiberPer100g(0.0).build());

        // ── Fruits ──
        items.add( FoodItem.builder().name("Banana").category("Fruits").caloriesPer100g(89.0).proteinPer100g(1.1).carbsPer100g(23.0).fatPer100g(0.3).fiberPer100g(2.6).build());
        items.add( FoodItem.builder().name("Apple").category("Fruits").caloriesPer100g(52.0).proteinPer100g(0.3).carbsPer100g(14.0).fatPer100g(0.2).fiberPer100g(2.4).build());
        items.add( FoodItem.builder().name("Orange").category("Fruits").caloriesPer100g(47.0).proteinPer100g(0.9).carbsPer100g(12.0).fatPer100g(0.1).fiberPer100g(2.4).build());
        items.add( FoodItem.builder().name("Strawberries").category("Fruits").caloriesPer100g(32.0).proteinPer100g(0.7).carbsPer100g(8.0).fatPer100g(0.3).fiberPer100g(2.0).build());
        items.add( FoodItem.builder().name("Blueberries").category("Fruits").caloriesPer100g(57.0).proteinPer100g(0.7).carbsPer100g(14.0).fatPer100g(0.3).fiberPer100g(2.4).build());
        items.add( FoodItem.builder().name("Grapes").category("Fruits").caloriesPer100g(69.0).proteinPer100g(0.7).carbsPer100g(18.0).fatPer100g(0.2).fiberPer100g(0.9).build());
        items.add( FoodItem.builder().name("Watermelon").category("Fruits").caloriesPer100g(30.0).proteinPer100g(0.6).carbsPer100g(8.0).fatPer100g(0.2).fiberPer100g(0.4).build());
        items.add( FoodItem.builder().name("Pineapple").category("Fruits").caloriesPer100g(50.0).proteinPer100g(0.5).carbsPer100g(13.0).fatPer100g(0.1).fiberPer100g(1.4).build());
        items.add( FoodItem.builder().name("Mango").category("Fruits").caloriesPer100g(60.0).proteinPer100g(0.8).carbsPer100g(15.0).fatPer100g(0.4).fiberPer100g(1.6).build());
        items.add( FoodItem.builder().name("Avocado").category("Fruits").caloriesPer100g(160.0).proteinPer100g(2.0).carbsPer100g(9.0).fatPer100g(15.0).fiberPer100g(6.7).build());
        items.add( FoodItem.builder().name("Mixed Berries").category("Fruits").caloriesPer100g(48.0).proteinPer100g(0.7).carbsPer100g(11.0).fatPer100g(0.3).fiberPer100g(2.2).build());
        items.add( FoodItem.builder().name("Kiwi").category("Fruits").caloriesPer100g(61.0).proteinPer100g(1.1).carbsPer100g(15.0).fatPer100g(0.5).fiberPer100g(3.0).build());

        // ── Vegetables ──
        items.add( FoodItem.builder().name("Broccoli").category("Vegetables").caloriesPer100g(34.0).proteinPer100g(2.8).carbsPer100g(7.0).fatPer100g(0.4).fiberPer100g(2.6).build());
        items.add( FoodItem.builder().name("Spinach").category("Vegetables").caloriesPer100g(23.0).proteinPer100g(2.9).carbsPer100g(3.6).fatPer100g(0.4).fiberPer100g(2.2).build());
        items.add( FoodItem.builder().name("Kale").category("Vegetables").caloriesPer100g(49.0).proteinPer100g(4.3).carbsPer100g(9.0).fatPer100g(0.9).fiberPer100g(4.1).build());
        items.add( FoodItem.builder().name("Carrots").category("Vegetables").caloriesPer100g(41.0).proteinPer100g(0.9).carbsPer100g(10.0).fatPer100g(0.2).fiberPer100g(2.8).build());
        items.add( FoodItem.builder().name("Tomato").category("Vegetables").caloriesPer100g(18.0).proteinPer100g(0.9).carbsPer100g(3.9).fatPer100g(0.2).fiberPer100g(1.2).build());
        items.add( FoodItem.builder().name("Bell Pepper").category("Vegetables").caloriesPer100g(31.0).proteinPer100g(1.0).carbsPer100g(6.0).fatPer100g(0.3).fiberPer100g(2.1).build());
        items.add( FoodItem.builder().name("Onion").category("Vegetables").caloriesPer100g(40.0).proteinPer100g(1.1).carbsPer100g(9.0).fatPer100g(0.1).fiberPer100g(1.7).build());
        items.add( FoodItem.builder().name("Garlic").category("Vegetables").caloriesPer100g(149.0).proteinPer100g(6.4).carbsPer100g(33.0).fatPer100g(0.5).fiberPer100g(2.1).build());
        items.add( FoodItem.builder().name("Cucumber").category("Vegetables").caloriesPer100g(15.0).proteinPer100g(0.7).carbsPer100g(3.6).fatPer100g(0.1).fiberPer100g(0.5).build());
        items.add( FoodItem.builder().name("Lettuce (Iceberg)").category("Vegetables").caloriesPer100g(14.0).proteinPer100g(0.9).carbsPer100g(3.0).fatPer100g(0.1).fiberPer100g(1.2).build());
        items.add( FoodItem.builder().name("Cauliflower").category("Vegetables").caloriesPer100g(25.0).proteinPer100g(1.9).carbsPer100g(5.0).fatPer100g(0.3).fiberPer100g(2.0).build());
        items.add( FoodItem.builder().name("Zucchini").category("Vegetables").caloriesPer100g(17.0).proteinPer100g(1.2).carbsPer100g(3.1).fatPer100g(0.3).fiberPer100g(1.0).build());
        items.add( FoodItem.builder().name("Sweet Potato").category("Vegetables").caloriesPer100g(86.0).proteinPer100g(1.6).carbsPer100g(20.0).fatPer100g(0.1).fiberPer100g(3.0).build());
        items.add( FoodItem.builder().name("Potato (White)").category("Vegetables").caloriesPer100g(77.0).proteinPer100g(2.0).carbsPer100g(17.0).fatPer100g(0.1).fiberPer100g(2.2).build());
        items.add( FoodItem.builder().name("Mushrooms").category("Vegetables").caloriesPer100g(22.0).proteinPer100g(3.1).carbsPer100g(3.3).fatPer100g(0.3).fiberPer100g(1.0).build());
        items.add( FoodItem.builder().name("Asparagus").category("Vegetables").caloriesPer100g(20.0).proteinPer100g(2.2).carbsPer100g(4.0).fatPer100g(0.1).fiberPer100g(2.1).build());
        items.add( FoodItem.builder().name("Green Beans").category("Vegetables").caloriesPer100g(31.0).proteinPer100g(1.8).carbsPer100g(7.0).fatPer100g(0.2).fiberPer100g(2.7).build());
        items.add( FoodItem.builder().name("Corn").category("Vegetables").caloriesPer100g(96.0).proteinPer100g(3.4).carbsPer100g(21.0).fatPer100g(1.5).fiberPer100g(2.4).build());

        // ── Grains & Cereals ──
        items.add( FoodItem.builder().name("White Rice (Cooked)").category("Grains & Cereals").caloriesPer100g(130.0).proteinPer100g(2.7).carbsPer100g(28.0).fatPer100g(0.3).fiberPer100g(0.4).build());
        items.add( FoodItem.builder().name("Brown Rice (Cooked)").category("Grains & Cereals").caloriesPer100g(123.0).proteinPer100g(2.7).carbsPer100g(26.0).fatPer100g(1.0).fiberPer100g(1.6).build());
        items.add( FoodItem.builder().name("Oatmeal (Rolled Oats)").category("Grains & Cereals").caloriesPer100g(389.0).proteinPer100g(17.0).carbsPer100g(66.0).fatPer100g(7.0).fiberPer100g(10.6).build());
        items.add( FoodItem.builder().name("Whole Wheat Bread").category("Grains & Cereals").caloriesPer100g(247.0).proteinPer100g(13.0).carbsPer100g(41.0).fatPer100g(3.4).fiberPer100g(7.0).build());
        items.add( FoodItem.builder().name("White Bread").category("Grains & Cereals").caloriesPer100g(265.0).proteinPer100g(9.0).carbsPer100g(49.0).fatPer100g(3.2).fiberPer100g(2.7).build());
        items.add( FoodItem.builder().name("Pasta (Cooked)").category("Grains & Cereals").caloriesPer100g(131.0).proteinPer100g(5.0).carbsPer100g(25.0).fatPer100g(1.1).fiberPer100g(1.8).build());
        items.add( FoodItem.builder().name("Quinoa (Cooked)").category("Grains & Cereals").caloriesPer100g(120.0).proteinPer100g(4.4).carbsPer100g(21.0).fatPer100g(1.9).fiberPer100g(2.8).build());
        items.add( FoodItem.builder().name("Couscous (Cooked)").category("Grains & Cereals").caloriesPer100g(112.0).proteinPer100g(3.8).carbsPer100g(23.0).fatPer100g(0.2).fiberPer100g(1.4).build());
        items.add( FoodItem.builder().name("Granola").category("Grains & Cereals").caloriesPer100g(471.0).proteinPer100g(10.0).carbsPer100g(64.0).fatPer100g(21.0).fiberPer100g(5.0).build());
        items.add( FoodItem.builder().name("Tortilla (Corn)").category("Grains & Cereals").caloriesPer100g(218.0).proteinPer100g(5.7).carbsPer100g(45.0).fatPer100g(2.5).fiberPer100g(4.0).build());

        // ── Legumes & Nuts ──
        items.add( FoodItem.builder().name("Black Beans (Cooked)").category("Legumes & Nuts").caloriesPer100g(132.0).proteinPer100g(8.9).carbsPer100g(24.0).fatPer100g(0.5).fiberPer100g(8.7).build());
        items.add( FoodItem.builder().name("Lentils (Cooked)").category("Legumes & Nuts").caloriesPer100g(116.0).proteinPer100g(9.0).carbsPer100g(20.0).fatPer100g(0.4).fiberPer100g(7.9).build());
        items.add( FoodItem.builder().name("Chickpeas (Cooked)").category("Legumes & Nuts").caloriesPer100g(139.0).proteinPer100g(7.6).carbsPer100g(22.0).fatPer100g(2.6).fiberPer100g(7.6).build());
        items.add( FoodItem.builder().name("Kidney Beans (Cooked)").category("Legumes & Nuts").caloriesPer100g(127.0).proteinPer100g(8.7).carbsPer100g(23.0).fatPer100g(0.5).fiberPer100g(6.4).build());
        items.add( FoodItem.builder().name("Almonds").category("Legumes & Nuts").caloriesPer100g(579.0).proteinPer100g(21.0).carbsPer100g(22.0).fatPer100g(50.0).fiberPer100g(12.5).build());
        items.add( FoodItem.builder().name("Peanuts").category("Legumes & Nuts").caloriesPer100g(567.0).proteinPer100g(26.0).carbsPer100g(16.0).fatPer100g(49.0).fiberPer100g(8.5).build());
        items.add( FoodItem.builder().name("Walnuts").category("Legumes & Nuts").caloriesPer100g(654.0).proteinPer100g(15.0).carbsPer100g(14.0).fatPer100g(65.0).fiberPer100g(6.7).build());
        items.add( FoodItem.builder().name("Cashews").category("Legumes & Nuts").caloriesPer100g(553.0).proteinPer100g(18.0).carbsPer100g(30.0).fatPer100g(44.0).fiberPer100g(3.3).build());
        items.add( FoodItem.builder().name("Chia Seeds").category("Legumes & Nuts").caloriesPer100g(486.0).proteinPer100g(17.0).carbsPer100g(42.0).fatPer100g(31.0).fiberPer100g(34.4).build());
        items.add( FoodItem.builder().name("Peanut Butter").category("Legumes & Nuts").caloriesPer100g(588.0).proteinPer100g(25.0).carbsPer100g(20.0).fatPer100g(50.0).fiberPer100g(6.0).build());
        items.add( FoodItem.builder().name("Tofu (Firm)").category("Legumes & Nuts").caloriesPer100g(76.0).proteinPer100g(8.0).carbsPer100g(1.9).fatPer100g(4.8).fiberPer100g(0.3).build());

        // ── Oils & Fats ──
        items.add( FoodItem.builder().name("Olive Oil").category("Oils & Fats").caloriesPer100g(884.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(100.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Coconut Oil").category("Oils & Fats").caloriesPer100g(862.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(100.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Canola Oil").category("Oils & Fats").caloriesPer100g(884.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(100.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Sesame Oil").category("Oils & Fats").caloriesPer100g(884.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(100.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Ghee").category("Oils & Fats").caloriesPer100g(876.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(99.5).fiberPer100g(0.0).build());

        // ── Sauces & Condiments ──
        items.add( FoodItem.builder().name("Soy Sauce").category("Sauces & Condiments").caloriesPer100g(53.0).proteinPer100g(8.1).carbsPer100g(4.7).fatPer100g(0.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Ketchup").category("Sauces & Condiments").caloriesPer100g(101.0).proteinPer100g(1.0).carbsPer100g(26.0).fatPer100g(0.1).fiberPer100g(0.3).build());
        items.add( FoodItem.builder().name("Mustard (Yellow)").category("Sauces & Condiments").caloriesPer100g(66.0).proteinPer100g(4.0).carbsPer100g(6.0).fatPer100g(3.0).fiberPer100g(1.5).build());
        items.add( FoodItem.builder().name("Mayonnaise").category("Sauces & Condiments").caloriesPer100g(700.0).proteinPer100g(1.0).carbsPer100g(0.7).fatPer100g(77.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Hot Sauce").category("Sauces & Condiments").caloriesPer100g(11.0).proteinPer100g(0.5).carbsPer100g(2.0).fatPer100g(0.4).fiberPer100g(0.5).build());
        items.add( FoodItem.builder().name("Vinaigrette Dressing").category("Sauces & Condiments").caloriesPer100g(290.0).proteinPer100g(0.5).carbsPer100g(8.0).fatPer100g(28.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Ranch Dressing").category("Sauces & Condiments").caloriesPer100g(430.0).proteinPer100g(1.5).carbsPer100g(6.0).fatPer100g(44.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("BBQ Sauce").category("Sauces & Condiments").caloriesPer100g(135.0).proteinPer100g(0.8).carbsPer100g(33.0).fatPer100g(0.5).fiberPer100g(0.5).build());

        // ── Snacks & Sweets ──
        items.add( FoodItem.builder().name("Dark Chocolate (70%)").category("Snacks & Sweets").caloriesPer100g(598.0).proteinPer100g(7.8).carbsPer100g(46.0).fatPer100g(43.0).fiberPer100g(11.0).build());
        items.add( FoodItem.builder().name("Milk Chocolate").category("Snacks & Sweets").caloriesPer100g(535.0).proteinPer100g(7.6).carbsPer100g(59.0).fatPer100g(30.0).fiberPer100g(1.5).build());
        items.add( FoodItem.builder().name("Potato Chips").category("Snacks & Sweets").caloriesPer100g(536.0).proteinPer100g(7.0).carbsPer100g(53.0).fatPer100g(34.0).fiberPer100g(3.0).build());
        items.add( FoodItem.builder().name("Mixed Nuts (Roasted)").category("Snacks & Sweets").caloriesPer100g(607.0).proteinPer100g(20.0).carbsPer100g(20.0).fatPer100g(54.0).fiberPer100g(7.0).build());
        items.add( FoodItem.builder().name("Trail Mix").category("Snacks & Sweets").caloriesPer100g(462.0).proteinPer100g(10.0).carbsPer100g(52.0).fatPer100g(28.0).fiberPer100g(6.5).build());
        items.add( FoodItem.builder().name("Honey").category("Snacks & Sweets").caloriesPer100g(304.0).proteinPer100g(0.3).carbsPer100g(82.0).fatPer100g(0.0).fiberPer100g(0.2).build());
        items.add( FoodItem.builder().name("Maple Syrup").category("Snacks & Sweets").caloriesPer100g(260.0).proteinPer100g(0.0).carbsPer100g(67.0).fatPer100g(0.1).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Vanilla Ice Cream").category("Snacks & Sweets").caloriesPer100g(207.0).proteinPer100g(3.5).carbsPer100g(24.0).fatPer100g(11.0).fiberPer100g(0.5).build());

        // ── Beverages ──
        items.add( FoodItem.builder().name("Orange Juice").category("Beverages").caloriesPer100g(45.0).proteinPer100g(0.7).carbsPer100g(10.0).fatPer100g(0.2).fiberPer100g(0.2).build());
        items.add( FoodItem.builder().name("Apple Juice").category("Beverages").caloriesPer100g(46.0).proteinPer100g(0.1).carbsPer100g(11.0).fatPer100g(0.1).fiberPer100g(0.1).build());
        items.add( FoodItem.builder().name("Coconut Water").category("Beverages").caloriesPer100g(19.0).proteinPer100g(0.7).carbsPer100g(3.7).fatPer100g(0.2).fiberPer100g(1.1).build());
        items.add( FoodItem.builder().name("Green Tea").category("Beverages").caloriesPer100g(1.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(0.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Black Coffee").category("Beverages").caloriesPer100g(1.0).proteinPer100g(0.1).carbsPer100g(0.0).fatPer100g(0.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Milk Coffee (Latte)").category("Beverages").caloriesPer100g(25.0).proteinPer100g(1.2).carbsPer100g(2.5).fatPer100g(1.1).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Chocolate Milk").category("Beverages").caloriesPer100g(83.0).proteinPer100g(3.3).carbsPer100g(10.0).fatPer100g(3.4).fiberPer100g(0.5).build());
        items.add( FoodItem.builder().name("Protein Shake (Whey)").category("Beverages").caloriesPer100g(55.0).proteinPer100g(10.0).carbsPer100g(2.0).fatPer100g(0.7).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Soda (Cola)").category("Beverages").caloriesPer100g(41.0).proteinPer100g(0.0).carbsPer100g(10.6).fatPer100g(0.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Diet Soda").category("Beverages").caloriesPer100g(0.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(0.0).fiberPer100g(0.0).build());

        // ── Processed & Prepared Foods ──
        items.add( FoodItem.builder().name("Pizza (Cheese)").category("Processed & Prepared").caloriesPer100g(266.0).proteinPer100g(11.0).carbsPer100g(33.0).fatPer100g(10.0).fiberPer100g(2.3).build());
        items.add( FoodItem.builder().name("Hamburger").category("Processed & Prepared").caloriesPer100g(252.0).proteinPer100g(17.0).carbsPer100g(25.0).fatPer100g(10.0).fiberPer100g(1.0).build());
        items.add( FoodItem.builder().name("French Fries").category("Processed & Prepared").caloriesPer100g(312.0).proteinPer100g(3.4).carbsPer100g(41.0).fatPer100g(15.0).fiberPer100g(3.8).build());
        items.add( FoodItem.builder().name("Chicken Nuggets").category("Processed & Prepared").caloriesPer100g(296.0).proteinPer100g(15.0).carbsPer100g(18.0).fatPer100g(18.0).fiberPer100g(0.2).build());
        items.add( FoodItem.builder().name("Hot Dog").category("Processed & Prepared").caloriesPer100g(247.0).proteinPer100g(10.0).carbsPer100g(18.0).fatPer100g(15.0).fiberPer100g(0.5).build());
        items.add( FoodItem.builder().name("Grilled Cheese").category("Processed & Prepared").caloriesPer100g(330.0).proteinPer100g(12.0).carbsPer100g(30.0).fatPer100g(18.0).fiberPer100g(1.5).build());
        items.add( FoodItem.builder().name("Caesar Salad").category("Processed & Prepared").caloriesPer100g(107.0).proteinPer100g(5.0).carbsPer100g(5.0).fatPer100g(8.0).fiberPer100g(2.0).build());

        // ── Supplements ──
        items.add( FoodItem.builder().name("Whey Protein Powder").category("Supplements").caloriesPer100g(395.0).proteinPer100g(80.0).carbsPer100g(5.0).fatPer100g(5.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Casein Protein Powder").category("Supplements").caloriesPer100g(380.0).proteinPer100g(75.0).carbsPer100g(6.0).fatPer100g(4.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Mass Gainer").category("Supplements").caloriesPer100g(400.0).proteinPer100g(25.0).carbsPer100g(65.0).fatPer100g(5.0).fiberPer100g(1.0).build());
        items.add( FoodItem.builder().name("Creatine Monohydrate").category("Supplements").caloriesPer100g(0.0).proteinPer100g(0.0).carbsPer100g(0.0).fatPer100g(0.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("BCAA Powder").category("Supplements").caloriesPer100g(200.0).proteinPer100g(50.0).carbsPer100g(0.0).fatPer100g(0.0).fiberPer100g(0.0).build());
        items.add( FoodItem.builder().name("Protein Bar").category("Supplements").caloriesPer100g(360.0).proteinPer100g(30.0).carbsPer100g(40.0).fatPer100g(10.0).fiberPer100g(5.0).build());

        return items;
    }
}
