import create from 'zustand';
import axios from 'axios';

const useChatStore = create((set, get) => ({
  chats: [],
  activeChat: null,
  messages: [],
  isLoadingChats: false,
  isLoadingMessages: false,
  error: null,
  nextPage: 0,
  hasMoreMessages: true,
  
  // Chats
  fetchChats: async () => {
    try {
      set({ isLoadingChats: true });
      const res = await axios.get('/api/chats');
      
      set({ 
        chats: res.data, 
        isLoadingChats: false,
        error: null
      });
    } catch (error) {
      set({ 
        isLoadingChats: false, 
        error: error.response?.data?.message || 'Failed to load chats'
      });
    }
  },
  
  fetchChat: async (chatId) => {
    try {
      set({ isLoadingChats: true });
      const res = await axios.get(`/api/chats/${chatId}`);
      
      set(state => {
        const updatedChats = state.chats.map(chat => 
          chat.id === res.data.id ? res.data : chat
        );
        
        return { 
          activeChat: res.data,
          chats: updatedChats,
          isLoadingChats: false,
          error: null
        };
      });
    } catch (error) {
      set({ 
        isLoadingChats: false, 
        error: error.response?.data?.message || 'Failed to load chat'
      });
    }
  },
  
  createChat: async (chatData) => {
    try {
      set({ isLoadingChats: true });
      const res = await axios.post('/api/chats', chatData);
      
      set(state => ({ 
        chats: [...state.chats, res.data], 
        activeChat: res.data,
        isLoadingChats: false,
        error: null
      }));
      
      return res.data;
    } catch (error) {
      set({ 
        isLoadingChats: false, 
        error: error.response?.data?.message || 'Failed to create chat'
      });
      return null;
    }
  },
  
  updateChat: async (chatId, updates) => {
    try {
      const res = await axios.put(`/api/chats/${chatId}`, updates);
      
      set(state => {
        const updatedChats = state.chats.map(chat => 
          chat.id === chatId ? res.data : chat
        );
        
        return {
          chats: updatedChats,
          activeChat: state.activeChat?.id === chatId ? res.data : state.activeChat,
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to update chat'
      });
      return false;
    }
  },
  
  deleteChat: async (chatId) => {
    try {
      await axios.delete(`/api/chats/${chatId}`);
      
      set(state => {
        const updatedChats = state.chats.filter(chat => chat.id !== chatId);
        return { 
          chats: updatedChats,
          activeChat: state.activeChat?.id === chatId ? null : state.activeChat,
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to delete chat'
      });
      return false;
    }
  },
  
  getOrCreateDirectChat: async (userId) => {
    try {
      set({ isLoadingChats: true });
      const res = await axios.get(`/api/chats/direct/${userId}`);
      
      // Check if chat already exists in our list
      let chatExists = false;
      set(state => {
        const existingChatIndex = state.chats.findIndex(c => c.id === res.data.id);
        
        if (existingChatIndex === -1) {
          // Chat doesn't exist in our list, add it
          return { 
            chats: [...state.chats, res.data],
            activeChat: res.data,
            isLoadingChats: false,
            error: null
          };
        } else {
          // Chat exists, update it
          const updatedChats = [...state.chats];
          updatedChats[existingChatIndex] = res.data;
          
          return {
            chats: updatedChats,
            activeChat: res.data,
            isLoadingChats: false,
            error: null
          };
        }
      });
      
      return res.data;
    } catch (error) {
      set({ 
        isLoadingChats: false, 
        error: error.response?.data?.message || 'Failed to get or create direct chat'
      });
      return null;
    }
  },
  
  setActiveChat: (chat) => {
    set({ activeChat: chat, messages: [], nextPage: 0, hasMoreMessages: true });
  },
  
  // Messages
  fetchMessages: async (chatId, page = 0, size = 50) => {
    try {
      set({ isLoadingMessages: true });
      
      const res = await axios.get(`/api/messages/chat/${chatId}?page=${page}&size=${size}`);
      const newMessages = res.data.content;
      const hasMore = !res.data.last;
      
      set(state => ({
        messages: page === 0 ? newMessages : [...state.messages, ...newMessages],
        nextPage: page + 1,
        hasMoreMessages: hasMore,
        isLoadingMessages: false,
        error: null
      }));
    } catch (error) {
      set({ 
        isLoadingMessages: false, 
        error: error.response?.data?.message || 'Failed to load messages'
      });
    }
  },
  
  sendMessage: async (messageData) => {
    try {
      const res = await axios.post('/api/messages', messageData);
      
      // We're not adding the message to the list here because it will come through WebSocket
      return res.data;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to send message'
      });
      return null;
    }
  },
  
  updateMessage: async (messageId, updates) => {
    try {
      const res = await axios.put(`/api/messages/${messageId}`, updates);
      
      set(state => {
        const updatedMessages = state.messages.map(msg => 
          msg.id === messageId ? res.data : msg
        );
        
        return { 
          messages: updatedMessages,
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to update message'
      });
      return false;
    }
  },
  
  deleteMessage: async (messageId) => {
    try {
      await axios.delete(`/api/messages/${messageId}`);
      
      set(state => {
        const updatedMessages = state.messages.filter(msg => msg.id !== messageId);
        return { 
          messages: updatedMessages,
          error: null
        };
      });
      
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to delete message'
      });
      return false;
    }
  },
  
  addReaction: async (messageId, emoji) => {
    try {
      await axios.post(`/api/messages/${messageId}/reaction`, { emoji });
      return true;
    } catch (error) {
      set({ 
        error: error.response?.data?.message || 'Failed to add reaction'
      });
      return false;
    }
  },
  
  markMessageAsRead: async (messageId) => {
    try {
      await axios.post(`/api/messages/${messageId}/read`);
      return true;
    } catch (error) {
      console.error('Failed to mark message as read:', error);
      return false;
    }
  },
  
  // WebSocket message handlers
  addMessage: (message) => {
    set(state => {
      // Add message only if it's for the active chat
      if (state.activeChat && message.chatId === state.activeChat.id) {
        return {
          messages: [message, ...state.messages]
        };
      }
      return state;
    });
  },
  
  updateChatLastMessage: (message) => {
    set(state => {
      const updatedChats = state.chats.map(chat => {
        if (chat.id === message.chatId) {
          return {
            ...chat,
            lastMessage: message,
            // Increment unread count if this chat is not active
            unreadCount: state.activeChat?.id === chat.id ? 0 : (chat.unreadCount || 0) + 1
          };
        }
        return chat;
      });
      
      return { chats: updatedChats };
    });
  },
  
  markChatAsRead: (chatId) => {
    set(state => {
      const updatedChats = state.chats.map(chat => {
        if (chat.id === chatId) {
          return {
            ...chat,
            unreadCount: 0
          };
        }
        return chat;
      });
      
      return { chats: updatedChats };
    });
  },
  
  // Reactions and typing indicators
  updateMessageReaction: (messageId, reactions) => {
    set(state => {
      const updatedMessages = state.messages.map(msg => {
        if (msg.id === messageId) {
          return {
            ...msg,
            reactions
          };
        }
        return msg;
      });
      
      return { messages: updatedMessages };
    });
  },
  
  // Errors
  clearError: () => set({ error: null }),
}));

export default useChatStore;