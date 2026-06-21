import { ApiResponse, DailyNutritionSummary, MacroDistribution, FoodLog } from '../types';
import { mockDailyDashboard } from './mockData';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const nutritionApi = {
  logFood: async (data: any) => {
    await delay(1000);
    return { success: true, data: { ...data, id: Math.random() } } as ApiResponse<FoodLog>;
  },
  getDailyAnalytics: async (date: string) => {
    await delay(800);
    return { success: true, data: mockDailyDashboard.nutritionSummary } as ApiResponse<DailyNutritionSummary>;
  },
  getMacros: async (from: string, to: string) => {
    return { success: true, data: null as any } as any;
  }
};
