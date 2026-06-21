import { AuthResponse, ApiResponse } from '../types';
import { api } from './axios';

export const authApi = {
  login: async (data: any) => {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return res.data;
  },
  register: async (data: any) => {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return res.data;
  }
};
