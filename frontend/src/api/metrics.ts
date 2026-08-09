import { ApiResponse } from '../types';
import { api } from './axios';

export interface SleepEntry {
  id: number;
  date: string;
  durationHours: number;
  qualityScore: number;
  bedtime?: string;
  wakeTime?: string;
}

export interface WaterEntry {
  id: number;
  date: string;
  amountMl: number;
}

export const metricApi = {
  recordWeight: async (weightKg: number) => {
    const res = await api.post<ApiResponse<any>>('/users/weight', { weightKg });
    return res.data;
  },
  getWeightTrend: async () => {
    const res = await api.get<ApiResponse<any[]>>('/metrics/trends/weight');
    return res.data;
  },
  recordSleep: async (data: { date: string; durationHours: number; qualityScore?: number }) => {
    const res = await api.post<ApiResponse<any>>('/sleep', data);
    return res.data;
  },
  getSleepHistory: async (days = 7) => {
    const res = await api.get<ApiResponse<SleepEntry[]>>(`/sleep/history?days=${days}`);
    return res.data;
  },
  recordWater: async (data: { date: string; amountMl: number }) => {
    const res = await api.post<ApiResponse<any>>('/water', data);
    return res.data;
  },
  getWaterHistory: async (days = 7) => {
    const res = await api.get<ApiResponse<WaterEntry[]>>(`/water/history?days=${days}`);
    return res.data;
  },
};
