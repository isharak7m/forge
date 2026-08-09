export const mockUser = {
  id: 1,
  name: 'Alex Fitness',
  email: 'alex@example.com',
  age: 28,
  gender: 'MALE',
  heightCm: 180,
  currentWeightKg: 82,
  goalWeightKg: 78,
  activityLevel: 'VERY_ACTIVE',
  fitnessGoal: 'FAT_LOSS',
  role: 'USER',
  createdAt: new Date().toISOString(),
};

export const mockDailyDashboard = {
  date: new Date().toISOString().split('T')[0],
  caloriesConsumed: 2150,
  caloriesBurned: 650,
  workoutsCompleted: 1,
  sleepHours: 7.5,
  waterLiters: 3.2,
  nutritionSummary: {
    date: new Date().toISOString().split('T')[0],
    totalCalories: 2150,
    totalProtein: 160,
    totalCarbs: 220,
    totalFat: 65,
    totalFiber: 35,
    consistencyScore: 92,
    meals: [
      {
        id: 1,
        date: new Date().toISOString().split('T')[0],
        mealCategory: 'BREAKFAST',
        foodName: 'Oatmeal & Protein Shake',
        servingSize: 1,
        unit: 'serving',
        calories: 450,
        proteinG: 40,
        carbsG: 50,
        fatG: 10,
        fiberG: 8,
        loggedAt: new Date().toISOString(),
      },
      {
        id: 2,
        date: new Date().toISOString().split('T')[0],
        mealCategory: 'LUNCH',
        foodName: 'Chicken Breast & Rice',
        servingSize: 1,
        unit: 'serving',
        calories: 650,
        proteinG: 55,
        carbsG: 70,
        fatG: 15,
        fiberG: 5,
        loggedAt: new Date().toISOString(),
      },
      {
        id: 3,
        date: new Date().toISOString().split('T')[0],
        mealCategory: 'DINNER',
        foodName: 'Salmon & Sweet Potato',
        servingSize: 1,
        unit: 'serving',
        calories: 750,
        proteinG: 45,
        carbsG: 60,
        fatG: 30,
        fiberG: 12,
        loggedAt: new Date().toISOString(),
      },
      {
        id: 4,
        date: new Date().toISOString().split('T')[0],
        mealCategory: 'SNACK',
        foodName: 'Greek Yogurt',
        servingSize: 1,
        unit: 'serving',
        calories: 300,
        proteinG: 20,
        carbsG: 40,
        fatG: 10,
        fiberG: 10,
        loggedAt: new Date().toISOString(),
      },
    ],
  },
  workouts: [
    {
      id: 1,
      date: new Date().toISOString().split('T')[0],
      name: 'Upper Body Power',
      notes: 'Felt strong today',
      durationMinutes: 75,
      totalVolume: 8500,
      createdAt: new Date().toISOString(),
      exercises: [
        {
          id: 1,
          exerciseName: 'Bench Press',
          category: 'CHEST',
          sets: 4,
          reps: 5,
          weightKg: 100,
          rpe: 8,
          notes: '',
          volume: 2000,
        },
        {
          id: 2,
          exerciseName: 'Overhead Press',
          category: 'SHOULDERS',
          sets: 3,
          reps: 8,
          weightKg: 60,
          rpe: 8,
          notes: '',
          volume: 1440,
        },
        {
          id: 3,
          exerciseName: 'Pull-ups',
          category: 'BACK',
          sets: 4,
          reps: 10,
          weightKg: 82,
          rpe: 9,
          notes: 'Bodyweight',
          volume: 3280,
        },
      ],
    },
  ],
};

export const mockWeightTrend = Array.from({ length: 30 }).map((_, i) => {
  const date = new Date();
  date.setDate(date.getDate() - (29 - i));
  return {
    date: date.toISOString().split('T')[0],
    value: 85 - i * 0.1 + (Math.random() * 0.5 - 0.25),
    label: 'Weight',
  };
});

export const mockPrediction = {
  currentWeight: 82.1,
  predicted30Days: 80.5,
  predicted60Days: 79.0,
  predicted90Days: 77.8,
  trend: 'LOSING',
  confidence: 'HIGH',
  methodology: 'Linear Regression over 30 days data',
  keyFactors: [
    'Consistent caloric deficit of ~300 kcal',
    'High protein adherence',
    'Good sleep consistency',
  ],
};

export const mockPlateaus = [
  {
    type: 'STRENGTH',
    description: 'Bench Press 1RM has stagnated at 115kg for 3 weeks.',
    daysStagnant: 21,
    affectedMetric: 'Bench Press',
    severity: 'MEDIUM',
    detectedSince: new Date().toISOString(),
  },
];

export const mockAdherence = {
  overallScore: 88,
  workoutConsistency: 35,
  nutritionConsistency: 28,
  sleepConsistency: 15,
  riskLevel: 'LOW',
  interpretation: 'Excellent adherence.',
  improvementAreas: ['Sleep duration on weekends'],
};

export const mockRecommendations = [
  {
    category: 'TRAINING',
    title: 'Schedule a Deload',
    description: 'Reduce volume by 40%',
    reason: 'Strength plateau detected on Bench Press.',
    priority: 'HIGH',
    actionItem: 'Plan deload workouts',
  },
  {
    category: 'NUTRITION',
    title: 'Maintain Deficit',
    description: 'Keep current calories',
    reason: 'Weight loss trend is optimal.',
    priority: 'LOW',
    actionItem: 'Keep tracking',
  },
];

export const mockPRs = [
  {
    exerciseName: 'Squat',
    weightKg: 140,
    reps: 5,
    sets: 1,
    volume: 700,
    achievedDate: new Date().toISOString(),
    estimatedOneRepMax: 163,
  },
  {
    exerciseName: 'Bench Press',
    weightKg: 115,
    reps: 1,
    sets: 1,
    volume: 115,
    achievedDate: new Date().toISOString(),
    estimatedOneRepMax: 115,
  },
  {
    exerciseName: 'Deadlift',
    weightKg: 180,
    reps: 3,
    sets: 1,
    volume: 540,
    achievedDate: new Date().toISOString(),
    estimatedOneRepMax: 198,
  },
];
