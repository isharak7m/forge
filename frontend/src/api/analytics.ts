import { ApiResponse, DailyDashboard } from '../types';
import { mockDailyDashboard } from './mockData';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const analyticsApi = {
  getDailyDashboard: async (date: string) => {
    await delay(800);
    const res: ApiResponse<DailyDashboard> = {
      success: true,
      message: 'Success',
      timestamp: new Date().toISOString(),
      data: mockDailyDashboard
    };
    return res;
  },
  getWeeklyDashboard: async (week: number, year: number) => {
    return { success: true, data: null as any } as any;
  },
  getMonthlyDashboard: async (month: number, year: number) => {
    return { success: true, data: null as any } as any;
  },
  getAllTimeDashboard: async () => {
    return { success: true, data: null as any } as any;
  }
};
