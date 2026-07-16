import { ApiResponse, WorkoutSession, PersonalRecord, ProgressionPoint } from '../types';
import { api } from './axios';

export const workoutApi = {
  createSession: async (data: any) => {
    const res = await api.post<ApiResponse<WorkoutSession>>('/workouts/sessions', data);
    return res.data;
  },
  addExercise: async (sessionId: number, data: any) => {
    const res = await api.post<ApiResponse<any>>(`/workouts/sessions/${sessionId}/exercises`, data);
    return res.data;
  },
  getSessions: async (from: string, to: string) => {
    const res = await api.get<ApiResponse<WorkoutSession[]>>(`/workouts/sessions?from=${from}&to=${to}`);
    return res.data;
  },
  getPRs: async () => {
    const res = await api.get<ApiResponse<PersonalRecord[]>>('/workouts/exercises/prs');
    return res.data;
  },
  getProgression: async (exerciseName: string) => {
    const res = await api.get<ApiResponse<ProgressionPoint[]>>(`/workouts/exercises/${encodeURIComponent(exerciseName)}/progression`);
    return res.data;
  },
  predict1RM: async (exerciseName: string) => {
    const res = await api.get<ApiResponse<any>>(`/workouts/predict-1rm?exerciseName=${encodeURIComponent(exerciseName)}`);
    return res.data;
  }
};
