import create from 'zustand';
import axios from 'axios';
import { setAuthToken, removeAuthToken } from '../utils/auth';

const useAuthStore = create((set) => ({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  error: null,
  
  checkAuth: async () => {
    try {
      set({ isLoading: true });
      
      const token = localStorage.getItem('token');
      if (!token) {
        set({ isAuthenticated: false, isLoading: false, user: null });
        return;
      }
      
      setAuthToken(token);
      const res = await axios.get('/api/users/me');
      
      set({ 
        isAuthenticated: true, 
        isLoading: false, 
        user: res.data,
        error: null
      });
    } catch (error) {
      removeAuthToken();
      set({ 
        isAuthenticated: false, 
        isLoading: false, 
        user: null,
        error: error.response?.data?.message || 'Authentication failed'
      });
    }
  },
  
  login: async (username, password) => {
    try {
      set({ isLoading: true });
      
      const res = await axios.post('/api/auth/login', { username, password });
      const { token, id, username: name, email, roles } = res.data;
      
      localStorage.setItem('token', token);
      setAuthToken(token);
      
      set({ 
        isAuthenticated: true, 
        isLoading: false, 
        user: { id, username: name, email, roles },
        error: null
      });
      
      return true;
    } catch (error) {
      set({ 
        isLoading: false, 
        error: error.response?.data?.message || 'Login failed'
      });
      return false;
    }
  },
  
  register: async (username, email, password) => {
    try {
      set({ isLoading: true });
      
      const res = await axios.post('/api/auth/signup', { username, email, password });
      
      set({ 
        isLoading: false, 
        error: null
      });
      
      return true;
    } catch (error) {
      set({ 
        isLoading: false, 
        error: error.response?.data?.message || 'Registration failed'
      });
      return false;
    }
  },
  
  logout: async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      removeAuthToken();
      localStorage.removeItem('token');
      set({ 
        isAuthenticated: false, 
        user: null,
        error: null
      });
    }
  },
  
  updateProfile: async (updates) => {
    try {
      set({ isLoading: true });
      
      const res = await axios.put('/api/users/me', updates);
      
      set(state => ({ 
        isLoading: false, 
        user: { ...state.user, ...res.data },
        error: null
      }));
      
      return true;
    } catch (error) {
      set({ 
        isLoading: false, 
        error: error.response?.data?.message || 'Profile update failed'
      });
      return false;
    }
  },
  
  uploadProfilePicture: async (file) => {
    try {
      set({ isLoading: true });
      
      const formData = new FormData();
      formData.append('file', file);
      
      const res = await axios.post('/api/users/me/profile-picture', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      set(state => ({ 
        isLoading: false, 
        user: { ...state.user, profilePicture: res.data.url },
        error: null
      }));
      
      return true;
    } catch (error) {
      set({ 
        isLoading: false, 
        error: error.response?.data?.message || 'Profile picture upload failed'
      });
      return false;
    }
  },
  
  clearError: () => set({ error: null }),
}));

export default useAuthStore;