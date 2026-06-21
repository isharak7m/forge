import { ApiResponse, DailyNutritionSummary, MacroDistribution, FoodLog } from '../types';
import { api } from './axios';

export const nutritionApi = {
  logFood: async (data: any) => {
    const res = await api.post<ApiResponse<FoodLog>>('/nutrition/log', data);
    return res.data;
  },
  getDailyAnalytics: async (date: string) => {
    const res = await api.get<ApiResponse<DailyNutritionSummary>>('/nutrition/analytics/daily', { params: { date } });
    return res.data;
  },
  getMacros: async (from: string, to: string) => {
    const res = await api.get<ApiResponse<MacroDistribution>>('/nutrition/analytics/macros', { params: { from, to } });
    return res.data;
  },
  deleteLog: async (id: number) => {
    const res = await api.delete<ApiResponse<void>>(`/nutrition/log/${id}`);
    return res.data;
  },
  getLogs: async (date: string, category?: string) => {
    const params: any = { date };
    if (category) params.category = category;
    const res = await api.get<ApiResponse<FoodLog[]>>('/nutrition/logs', { params });
    return res.data;
  }
};
