import { ApiResponse, DailyDashboard, WeeklyDashboard, MonthlyDashboard, AllTimeDashboard } from '../types';
import { api } from './axios';

export const analyticsApi = {
  getDailyDashboard: async (date: string) => {
    const res = await api.get<ApiResponse<DailyDashboard>>('/analytics/dashboard/daily', { params: { date } });
    return res.data;
  },
  getWeeklyDashboard: async (week: number, year: number) => {
    const res = await api.get<ApiResponse<WeeklyDashboard>>('/analytics/dashboard/weekly', { params: { week, year } });
    return res.data;
  },
  getMonthlyDashboard: async (month: number, year: number) => {
    const res = await api.get<ApiResponse<MonthlyDashboard>>('/analytics/dashboard/monthly', { params: { month, year } });
    return res.data;
  },
  getAllTimeDashboard: async () => {
    const res = await api.get<ApiResponse<AllTimeDashboard>>('/analytics/dashboard/alltime');
    return res.data;
  }
};
