import create from 'zustand';
import axios from 'axios';

const useContactsStore = create((set) => ({
  contacts: [],
  blockedUsers: [],
  allUsers: [],
  isLoadingContacts: false,
  isLoadingAllUsers: false,
  error: null,
  
  fetchContacts: async () => {
    try {
      set({ isLoadingContacts: true });
      const res = await axios.get('/api/users/contacts');
      
      set({ 
        contacts: res.data, 
        isLoadingContacts: false,
        error: null
      });
    } catch (error) {
      set({ 
        isLoadingContacts: false, 
        error: error.response?.data?.message || 'Failed to load contacts'
      });
    }
  },
  
  fetchAllUsers: async () => {
    try {
      set({ isLoadingAllUsers: true });
      const res = await axios.get('/api/users');
      
      set({ 
        allUsers: res.data, 
        isLoadingAllUsers: false,
        error: null
      });
    } catch (error) {
      set({ 
        isLoadingAllUsers: false, 
        error: error.response?.data?.message || 'Failed to load users'
      });
    }
  },
  
  fetchBlockedUsers: async () => {
    try {
      set({ isLoadingContacts: true });
      const res = await axios.get('/api/users/blocked');
      
      set({ 
        blockedUsers: res.data, 
        isLoadingContacts: false,
        error: null
      });
    } catch (error) {
      set({ 
        isLoadingContacts: false, 
        error: error.response?.data?.message || 'Failed to load blocked users'
      });
    }
  },
  
  searchUsers: async (query) => {
    try {
      set({ isLoadingAllUsers: true });
      const res = await axios.get(`/api/users/search?query=${encodeURIComponent(query)}`);
      
      set({ 
        allUsers: res.data, 
        isLoadingAllUsers: false,
        error: null
      });
      
      return res.data;
    } catch (error) {
      set({ 
        isLoadingAllUsers: false, 
        error: error.response?.data?.message || 'Failed to search users'
      });
      return [];
    }
  },
  
  addContact: async (userId) => {
    try {
      await axios.post(`/api/users/contacts/${userId}`);
      
      // Refresh contacts
      const res = await axios.get('/api/users/contacts');
      set({ contacts: res.data, error: null });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to add contact'
      });
      return false;
    }
  },
  
  removeContact: async (userId) => {
    try {
      await axios.delete(`/api/users/contacts/${userId}`);
      
      set(state => {
        const updatedContacts = state.contacts.filter(contact => contact.id !== userId);
        return { contacts: updatedContacts, error: null };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to remove contact'
      });
      return false;
    }
  },
  
  blockUser: async (userId) => {
    try {
      await axios.post(`/api/users/block/${userId}`);
      
      // Refresh blocked users
      const res = await axios.get('/api/users/blocked');
      set({ blockedUsers: res.data, error: null });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to block user'
      });
      return false;
    }
  },
  
  unblockUser: async (userId) => {
    try {
      await axios.delete(`/api/users/block/${userId}`);
      
      set(state => {
        const updatedBlockedUsers = state.blockedUsers.filter(user => user.id !== userId);
        return { blockedUsers: updatedBlockedUsers, error: null };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to unblock user'
      });
      return false;
    }
  },
  
  // User presence status updates
  updateUserStatus: (userId, status) => {
    set(state => {
      const updateUser = (user) => {
        if (user.id === userId) {
          return { ...user, status };
        }
        return user;
      };
      
      return {
        contacts: state.contacts.map(updateUser),
        allUsers: state.allUsers.map(updateUser),
        blockedUsers: state.blockedUsers.map(updateUser)
      };
    });
  },
  
  // Errors
  clearError: () => set({ error: null }),
}));

export default useContactsStore;