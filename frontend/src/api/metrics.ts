import { ApiResponse, BodyMetric, TrendPoint } from '../types';
import { api } from './axios';

export const metricApi = {
  recordMetric: async (data: any) => {
    const res = await api.post<ApiResponse<BodyMetric>>('/metrics', data);
    return res.data;
  },
  getWeightTrend: async () => {
    const to = new Date().toISOString().split('T')[0];
    const from = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    const res = await api.get<ApiResponse<TrendPoint[]>>('/metrics/trends/weight', { params: { from, to } });
    return res.data;
  }
};
