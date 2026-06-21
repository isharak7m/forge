import { AuthResponse, ApiResponse } from '../types';
import { mockUser } from './mockData';

// Simulated delay
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const authApi = {
  login: async (data: any) => {
    await delay(1000);
    const res: ApiResponse<AuthResponse> = {
      success: true,
      message: 'Logged in successfully',
      timestamp: new Date().toISOString(),
      data: {
        token: 'mock-jwt-token-for-demo',
        tokenType: 'Bearer',
        user: mockUser
      }
    };
    return res;
  },
  register: async (data: any) => {
    await delay(1500);
    const res: ApiResponse<AuthResponse> = {
      success: true,
      message: 'Registered successfully',
      timestamp: new Date().toISOString(),
      data: {
        token: 'mock-jwt-token-for-demo',
        tokenType: 'Bearer',
        user: { ...mockUser, name: data.name, email: data.email }
      }
    };
    return res;
  }
};
