import {
  ApiResponse,
  WeightPrediction,
  PlateauAlert,
  AdherenceScore,
  Recommendation,
  WorkoutPrediction,
  RLRecommendation,
} from '../types';
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
  getWorkoutPrediction: async (exercise: string) => {
    const res = await api.get<ApiResponse<WorkoutPrediction>>('/ai/predict/workout', {
      params: { exercise },
    });
    return res.data;
  },
  getExercises: async () => {
    const res = await api.get<ApiResponse<string[]>>('/ai/predict/exercises');
    return res.data;
  },
  listTools: async () => {
    const res = await api.get<ApiResponse<Record<string, any>>>('/ai/tools');
    return res.data;
  },
  executeTool: async (tool: string, params: Record<string, string> = {}) => {
    const res = await api.post<ApiResponse<Record<string, string>>>('/ai/tools/execute', {
      tool,
      ...params,
    });
    return res.data;
  },
  queryTools: async (q: string) => {
    const res = await api.get<ApiResponse<any>>('/ai/tools/query', { params: { q } });
    return res.data;
  },
  getRLRecommend: async () => {
    const res = await api.get<ApiResponse<RLRecommendation>>('/ai/rl/recommend');
    return res.data;
  },
  submitRLFeedback: async (state: string, action: string, reward: number, nextState?: string) => {
    const res = await api.post<ApiResponse<string>>('/ai/rl/feedback', {
      state,
      action,
      reward,
      nextState,
    });
    return res.data;
  },
};
