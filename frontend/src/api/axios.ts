import axios from 'axios';
import { useAuthStore } from '../store/authStore';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL as string || 'https://forge-w40c.onrender.com/api',
  timeout: 60000,
});

api.interceptors.request.use((config) => {
  const state = useAuthStore.getState();
  if (state.token) {
    if (state.isTokenExpired()) {
      state.logout();
      window.location.href = '/login';
      return Promise.reject(new Error('Token expired'));
    }
    config.headers.Authorization = `Bearer ${state.token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
