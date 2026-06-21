import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User } from '../types';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  tokenExpiresAt: number | null;
  login: (token: string, user: User) => void;
  logout: () => void;
  setUser: (user: User) => void;
  isTokenExpired: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      tokenExpiresAt: null,
      login: (token, user) => {
        console.log('Storing user:', user);
        set({ token, user, isAuthenticated: true, tokenExpiresAt: Date.now() + 24 * 60 * 60 * 1000 });
      },
      logout: () => set({ token: null, user: null, isAuthenticated: false, tokenExpiresAt: null }),
      setUser: (user) => set({ user }),
      isTokenExpired: () => {
        const { tokenExpiresAt } = get();
        return tokenExpiresAt ? Date.now() > tokenExpiresAt : true;
      },
    }),
    {
      name: 'fitmind_auth',
    }
  )
);
