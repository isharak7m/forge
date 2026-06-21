import { ApiResponse, WeightPrediction, PlateauAlert, AdherenceScore, Recommendation } from '../types';
import { api } from './axios';

export const aiApi = {
  getPrediction: async () => {
    const res = await api.get<ApiResponse<WeightPrediction>>('/ai/predict');
    return res.data;
  },
  getPlateaus: async () => {
    const res = await api.get<ApiResponse<PlateauAlert[]>>('/ai/plateaus');
    return res.data;
  },
  getAdherence: async () => {
    const res = await api.get<ApiResponse<AdherenceScore>>('/ai/adherence');
    return res.data;
  },
  getRecommendations: async () => {
    const res = await api.get<ApiResponse<Recommendation[]>>('/ai/recommendations');
    return res.data;
  },
};
