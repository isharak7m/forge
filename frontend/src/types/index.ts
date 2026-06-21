export interface User {
  id: number;
  name: string;
  email: string;
  age?: number;
  gender?: string;
  heightCm?: number;
  currentWeightKg?: number;
  goalWeightKg?: number;
  activityLevel?: string;
  fitnessGoal?: string;
  role: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface FoodLog {
  id: number;
  date: string;
  mealCategory: string;
  foodName: string;
  servingSize: number;
  unit: string;
  calories: number;
  proteinG: number;
  carbsG: number;
  fatG: number;
  fiberG: number;
  loggedAt: string;
}

export interface ExerciseLog {
  id: number;
  exerciseName: string;
  category: string;
  sets: number;
  reps: number;
  weightKg: number;
  rpe: number;
  notes: string;
  volume: number;
}

export interface WorkoutSession {
  id: number;
  date: string;
  name: string;
  notes: string;
  durationMinutes: number;
  exercises: ExerciseLog[];
  totalVolume: number;
  createdAt: string;
}

export interface BodyMetric {
  id: number;
  recordedDate: string;
  weightKg: number;
  waistCm: number;
  chestCm: number;
  armsCm: number;
  thighsCm: number;
  bodyFatPercentage: number;
  sleepHours: number;
  waterLiters: number;
  recoveryScore: number;
  notes: string;
}

export interface DailyNutritionSummary {
  date: string;
  totalCalories: number;
  totalProtein: number;
  totalCarbs: number;
  totalFat: number;
  totalFiber: number;
  consistencyScore: number;
  meals: FoodLog[];
}

export interface MacroDistribution {
  totalCalories: number;
  totalProtein: number;
  totalCarbs: number;
  totalFat: number;
  proteinPct: number;
  carbsPct: number;
  fatPct: number;
  proteinCalories: number;
  carbCalories: number;
  fatCalories: number;
}

export interface WeightPrediction {
  currentWeight: number;
  predicted30Days: number;
  predicted60Days: number;
  predicted90Days: number;
  trend: string;
  confidence: string;
  methodology: string;
  keyFactors: string[];
}

export interface PlateauAlert {
  type: string;
  description: string;
  daysStagnant: number;
  affectedMetric: string;
  severity: string;
  detectedSince: string;
}

export interface AdherenceScore {
  overallScore: number;
  workoutConsistency: number;
  nutritionConsistency: number;
  sleepConsistency: number;
  riskLevel: string;
  interpretation: string;
  improvementAreas: string[];
}

export interface Recommendation {
  category: string;
  title: string;
  description: string;
  reason: string;
  priority: string;
  actionItem: string;
}

export interface AssistantResponse {
  query: string;
  response: string;
  intent: string;
  dataSources: string[];
  timestamp: string;
}

export interface TrendPoint {
  date: string;
  value: number;
  label: string;
}

export interface DailyDashboard {
  date: string;
  caloriesConsumed: number;
  caloriesBurned: number;
  workoutsCompleted: number;
  sleepHours: number;
  waterLiters: number;
  nutritionSummary: DailyNutritionSummary;
  workouts: WorkoutSession[];
}

export interface WeeklyDashboard {
  weekNumber: number;
  year: number;
  weightChange: number;
  totalVolume: number;
  volumeChangePercent: number;
  avgCalories: number;
  consistencyScore: number;
  weightTrend: TrendPoint[];
  calorieTrend: TrendPoint[];
}

export interface MonthlyDashboard {
  month: number;
  year: number;
  weightChange: number;
  avgCalories: number;
  nutritionAdherence: number;
  workoutAdherence: number;
  weightTrend: TrendPoint[];
  calorieTrend: TrendPoint[];
  exerciseProgressionSummary: Record<string, number>;
}

export interface PersonalRecord {
  exerciseName: string;
  weightKg: number;
  reps: number;
  sets: number;
  volume: number;
  achievedDate: string;
  estimatedOneRepMax: number;
}

export interface AllTimeDashboard {
  startWeight: number;
  currentWeight: number;
  totalWeightChange: number;
  totalWorkouts: number;
  totalVolume: number;
  totalCaloriesTracked: number;
  totalFoodLogsCount: number;
  bestLifts: Record<string, PersonalRecord>;
  firstWorkoutDate: string;
  memberSince: string;
}

export interface ProgressionPoint {
  date: string;
  volume: number;
  maxWeight: number;
  totalReps: number;
}
