DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'food_items' AND column_name = 'calories_per_100g') THEN
        ALTER TABLE food_items RENAME COLUMN calories_per_100g TO calories_per100g;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'food_items' AND column_name = 'protein_per_100g') THEN
        ALTER TABLE food_items RENAME COLUMN protein_per_100g TO protein_per100g;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'food_items' AND column_name = 'carbs_per_100g') THEN
        ALTER TABLE food_items RENAME COLUMN carbs_per_100g TO carbs_per100g;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'food_items' AND column_name = 'fat_per_100g') THEN
        ALTER TABLE food_items RENAME COLUMN fat_per_100g TO fat_per100g;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'food_items' AND column_name = 'fiber_per_100g') THEN
        ALTER TABLE food_items RENAME COLUMN fiber_per_100g TO fiber_per100g;
    END IF;
END $$;
