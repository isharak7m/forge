import { ApiResponse, User } from '../types';
import { api } from './axios';

export const userApi = {
  updateProfile: async (data: Partial<User>) => {
    const res = await api.put<ApiResponse<User>>('/users/me', data);
    return res.data;
  },
  getProfile: async () => {
    const res = await api.get<ApiResponse<User>>('/users/me');
    return res.data;
  },
};
