import { ApiResponse, BodyMetric, TrendPoint } from '../types';
import { mockWeightTrend } from './mockData';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const metricApi = {
  recordMetric: async (data: any) => {
    await delay(1000);
    return { success: true, data: { ...data, id: Math.random() } } as ApiResponse<BodyMetric>;
  },
  getWeightTrend: async () => {
    await delay(800);
    return { success: true, data: mockWeightTrend } as ApiResponse<TrendPoint[]>;
  }
};
