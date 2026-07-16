import { ApiResponse, DailyNutritionSummary, MacroDistribution, FoodLog } from '../types';
import { api } from './axios';

export const nutritionApi = {
  logFood: async (data: any) => {
    const res = await api.post<ApiResponse<FoodLog>>('/nutrition/log', data);
    return res.data;
  },
  deleteLog: async (id: number) => {
    const res = await api.delete<ApiResponse<void>>(`/nutrition/log/${id}`);
    return res.data;
  },
  getDailyAnalytics: async (date: string) => {
    const res = await api.get<ApiResponse<DailyNutritionSummary>>(`/nutrition/analytics/daily?date=${date}`);
    return res.data;
  },
  getMacros: async (from: string, to: string) => {
    const res = await api.get<ApiResponse<MacroDistribution>>(`/nutrition/analytics/macros?from=${from}&to=${to}`);
    return res.data;
  },
  searchFoods: async (query: string) => {
    const res = await api.get<ApiResponse<any[]>>(`/nutrition/search?query=${encodeURIComponent(query)}`);
    return res.data;
  }
};
