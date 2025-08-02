import create from 'zustand';
import axios from 'axios';

const useNotificationStore = create((set) => ({
  notifications: [],
  unreadCount: 0,
  isLoadingNotifications: false,
  error: null,
  
  fetchNotifications: async (page = 0, size = 20) => {
    try {
      set({ isLoadingNotifications: true });
      const res = await axios.get(`/api/notifications?page=${page}&size=${size}`);
      
      set({ 
        notifications: res.data.content, 
        isLoadingNotifications: false,
        error: null
      });
    } catch (error) {
      set({ 
        isLoadingNotifications: false, 
        error: error.response?.data?.message || 'Failed to load notifications'
      });
    }
  },
  
  fetchUnreadCount: async () => {
    try {
      const res = await axios.get('/api/notifications/count');
      
      set({ 
        unreadCount: res.data.count,
        error: null
      });
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
    }
  },
  
  markAsRead: async (notificationId) => {
    try {
      const res = await axios.post(`/api/notifications/${notificationId}/read`);
      
      set(state => {
        const updatedNotifications = state.notifications.map(notification => 
          notification.id === notificationId ? res.data : notification
        );
        
        return { 
          notifications: updatedNotifications,
          unreadCount: Math.max(0, state.unreadCount - 1),
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to mark notification as read'
      });
      return false;
    }
  },
  
  markAllAsRead: async () => {
    try {
      await axios.post('/api/notifications/read-all');
      
      set(state => {
        const updatedNotifications = state.notifications.map(notification => ({
          ...notification,
          read: true,
          readAt: new Date().toISOString()
        }));
        
        return { 
          notifications: updatedNotifications,
          unreadCount: 0,
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to mark all notifications as read'
      });
      return false;
    }
  },
  
  deleteNotification: async (notificationId) => {
    try {
      await axios.delete(`/api/notifications/${notificationId}`);
      
      set(state => {
        const notification = state.notifications.find(n => n.id === notificationId);
        const wasUnread = notification && !notification.read;
        
        const updatedNotifications = state.notifications.filter(n => n.id !== notificationId);
        return { 
          notifications: updatedNotifications,
          unreadCount: wasUnread ? Math.max(0, state.unreadCount - 1) : state.unreadCount,
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to delete notification'
      });
      return false;
    }
  },
  
  deleteAllNotifications: async () => {
    try {
      await axios.delete('/api/notifications');
      
      set({ 
        notifications: [],
        unreadCount: 0,
        error: null
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to delete all notifications'
      });
      return false;
    }
  },
  
  // Add a new notification (for WebSocket updates)
  addNotification: (notification) => {
    set(state => ({
      notifications: [notification, ...state.notifications],
      unreadCount: state.unreadCount + 1
    }));
  },
  
  // Errors
  clearError: () => set({ error: null }),
}));

export default useNotificationStore;