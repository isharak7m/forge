export interface NutrientInfo {
  rda: number;
  unit: string;
  label: string;
}

export const NUTRITION_RDA: Record<string, NutrientInfo> = {
  vitaminA: { rda: 900, unit: 'mcg', label: 'Vitamin A' },
  vitaminC: { rda: 90, unit: 'mg', label: 'Vitamin C' },
  vitaminD: { rda: 20, unit: 'mcg', label: 'Vitamin D' },
  vitaminE: { rda: 15, unit: 'mg', label: 'Vitamin E' },
  vitaminK: { rda: 120, unit: 'mcg', label: 'Vitamin K' },
  vitaminB1: { rda: 1.2, unit: 'mg', label: 'Vitamin B1' },
  vitaminB2: { rda: 1.3, unit: 'mg', label: 'Vitamin B2' },
  vitaminB3: { rda: 16, unit: 'mg', label: 'Vitamin B3' },
  vitaminB5: { rda: 5, unit: 'mg', label: 'Vitamin B5' },
  vitaminB6: { rda: 1.3, unit: 'mg', label: 'Vitamin B6' },
  vitaminB7: { rda: 30, unit: 'mcg', label: 'Vitamin B7' },
  vitaminB9: { rda: 400, unit: 'mcg', label: 'Vitamin B9' },
  vitaminB12: { rda: 2.4, unit: 'mcg', label: 'Vitamin B12' },
  calcium: { rda: 1000, unit: 'mg', label: 'Calcium' },
  iron: { rda: 8, unit: 'mg', label: 'Iron' },
  magnesium: { rda: 420, unit: 'mg', label: 'Magnesium' },
  potassium: { rda: 4700, unit: 'mg', label: 'Potassium' },
  sodium: { rda: 2300, unit: 'mg', label: 'Sodium' },
  zinc: { rda: 11, unit: 'mg', label: 'Zinc' },
  copper: { rda: 0.9, unit: 'mg', label: 'Copper' },
  manganese: { rda: 2.3, unit: 'mg', label: 'Manganese' },
  selenium: { rda: 55, unit: 'mcg', label: 'Selenium' },
  phosphorus: { rda: 700, unit: 'mg', label: 'Phosphorus' },
  iodine: { rda: 150, unit: 'mcg', label: 'Iodine' },
  chromium: { rda: 35, unit: 'mcg', label: 'Chromium' },
};

export const MICRO_FIELDS = Object.keys(NUTRITION_RDA);

export function getMicroLabel(key: string): string {
  return NUTRITION_RDA[key]?.label ?? key;
}

export function getMicroUnit(key: string): string {
  return NUTRITION_RDA[key]?.unit ?? 'mg';
}

export function getMicroRda(key: string): number {
  return NUTRITION_RDA[key]?.rda ?? 100;
}
