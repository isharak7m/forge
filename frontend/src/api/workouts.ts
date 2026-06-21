import { ApiResponse, WorkoutSession, PersonalRecord, ProgressionPoint } from '../types';
import { mockDailyDashboard, mockPRs } from './mockData';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const workoutApi = {
  createSession: async (data: any) => {
    await delay(1000);
    return { success: true, data: { ...data, id: Math.random(), exercises: [], totalVolume: 0 } } as ApiResponse<WorkoutSession>;
  },
  addExercise: async (sessionId: number, data: any) => {
    return { success: true, data: null as any } as any;
  },
  getSessions: async (from: string, to: string) => {
    await delay(800);
    return { success: true, data: mockDailyDashboard.workouts } as ApiResponse<WorkoutSession[]>;
  },
  getPRs: async () => {
    await delay(600);
    return { success: true, data: mockPRs } as ApiResponse<PersonalRecord[]>;
  },
  getProgression: async (exerciseName: string) => {
    return { success: true, data: [] } as ApiResponse<ProgressionPoint[]>;
  }
};
