import { ApiResponse, WeightPrediction, PlateauAlert, AdherenceScore, Recommendation, AssistantResponse } from '../types';
import { mockPrediction, mockPlateaus, mockAdherence, mockRecommendations } from './mockData';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const aiApi = {
  getPrediction: async () => {
    await delay(500);
    return { success: true, data: mockPrediction } as ApiResponse<WeightPrediction>;
  },
  getPlateaus: async () => {
    await delay(600);
    return { success: true, data: mockPlateaus } as ApiResponse<PlateauAlert[]>;
  },
  getAdherence: async () => {
    await delay(700);
    return { success: true, data: mockAdherence } as ApiResponse<AdherenceScore>;
  },
  getRecommendations: async () => {
    await delay(800);
    return { success: true, data: mockRecommendations } as ApiResponse<Recommendation[]>;
  },
  askAssistant: async (query: string) => {
    await delay(1500);
    let response = "I'm a mock AI assistant. Since the backend is down, I can't generate a real response, but normally I would analyze your data and answer: " + query;
    if (query.toLowerCase().includes('predict')) {
       response = "Based on your 30-day weight trend, you are losing approximately 0.5kg per week. Your predicted weight next month is 80.5kg.";
    } else if (query.toLowerCase().includes('calorie')) {
       response = "You have averaged 2150 calories over the last 14 days, which is consistent with your fat loss goal.";
    }
    
    const mockRes: AssistantResponse = {
      query,
      response,
      intent: 'GENERAL_QUERY',
      dataSources: ['Mock DB (Backend Down)'],
      timestamp: new Date().toISOString()
    };
    return { success: true, data: mockRes } as ApiResponse<AssistantResponse>;
  }
};
