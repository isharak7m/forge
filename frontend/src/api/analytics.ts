import { ApiResponse, DailyDashboard } from '../types';
import { api } from './axios';

export const analyticsApi = {
  getDailyDashboard: async (date: string) => {
    const res = await api.get<ApiResponse<DailyDashboard>>(
      `/analytics/dashboard/daily?date=${date}`,
    );
    return res.data;
  },
  getWeeklyDashboard: async (week: number, year: number) => {
    const res = await api.get<ApiResponse<any>>(
      `/analytics/dashboard/weekly?week=${week}&year=${year}`,
    );
    return res.data;
  },
  getMonthlyDashboard: async (month: number, year: number) => {
    const res = await api.get<ApiResponse<any>>(
      `/analytics/dashboard/monthly?month=${month}&year=${year}`,
    );
    return res.data;
  },
  getAllTimeDashboard: async () => {
    const res = await api.get<ApiResponse<any>>(`/analytics/dashboard/alltime`);
    return res.data;
  },
};
