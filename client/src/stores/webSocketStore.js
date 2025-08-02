import create from 'zustand';
import SockJS from 'sockjs-client';
import { Stomp } from 'stompjs';
import useAuthStore from './authStore';
import useChatStore from './chatStore';
import useContactsStore from './contactsStore';
import useNotificationStore from './notificationStore';

const useWebSocketStore = create((set, get) => {
  let stompClient = null;
  
  return {
    connected: false,
    connecting: false,
    typingUsers: {},
    
    connect: () => {
      const { connected, connecting } = get();
      if (connected || connecting) return;
      
      set({ connecting: true });
      
      const socket = new SockJS('/ws');
      stompClient = Stomp.over(socket);
      
      // Disable debug logging
      stompClient.debug = null;
      
      const token = localStorage.getItem('token');
      const headers = {
        Authorization: `Bearer ${token}`
      };
      
      stompClient.connect(
        headers,
        () => {
          set({ connected: true, connecting: false });
          get().subscribeToPersonalTopics();
        },
        (error) => {
          console.error('WebSocket connection error:', error);
          set({ connected: false, connecting: false });
          
          // Try to reconnect after 5 seconds
          setTimeout(() => {
            get().connect();
          }, 5000);
        }
      );
    },
    
    disconnect: () => {
      if (stompClient) {
        stompClient.disconnect();
      }
      set({ connected: false, typingUsers: {} });
    },
    
    subscribeToPersonalTopics: () => {
      if (!stompClient || !get().connected) return;
      
      const user = useAuthStore.getState().user;
      const addMessage = useChatStore.getState().addMessage;
      const updateChatLastMessage = useChatStore.getState().updateChatLastMessage;
      const updateUserStatus = useContactsStore.getState().updateUserStatus;
      const addNotification = useNotificationStore.getState().addNotification;
      
      // Subscribe to personal queue for messages sent when offline
      stompClient.subscribe(`/user/${user.username}/queue/messages`, (message) => {
        const receivedMessage = JSON.parse(message.body);
        addMessage(receivedMessage);
        updateChatLastMessage(receivedMessage);
      });
      
      // Subscribe to user status updates
      stompClient.subscribe('/topic/users/status', (message) => {
        const statusUpdate = JSON.parse(message.body);
        updateUserStatus(statusUpdate.userId, statusUpdate.status);
      });
    },
    
    subscribeToChat: (chatId) => {
      if (!stompClient || !get().connected) return;
      
      const addMessage = useChatStore.getState().addMessage;
      const updateChatLastMessage = useChatStore.getState().updateChatLastMessage;
      const updateMessageReaction = useChatStore.getState().updateMessageReaction;
      
      // Subscribe to chat messages
      stompClient.subscribe(`/topic/chat/${chatId}`, (message) => {
        const receivedMessage = JSON.parse(message.body);
        addMessage(receivedMessage);
        updateChatLastMessage(receivedMessage);
      });
      
      // Subscribe to typing indicators
      stompClient.subscribe(`/topic/chat/${chatId}/typing`, (message) => {
        const typingEvent = JSON.parse(message.body);
        
        set(state => {
          // Clear after 3 seconds of inactivity
          if (state.typingUsers[typingEvent.userId]) {
            clearTimeout(state.typingUsers[typingEvent.userId].timeout);
          }
          
          const timeout = setTimeout(() => {
            set(innerState => {
              const updatedTypingUsers = { ...innerState.typingUsers };
              delete updatedTypingUsers[typingEvent.userId];
              return { typingUsers: updatedTypingUsers };
            });
          }, 3000);
          
          return {
            typingUsers: {
              ...state.typingUsers,
              [typingEvent.userId]: {
                username: typingEvent.username,
                chatId,
                timeout
              }
            }
          };
        });
      });
      
      // Subscribe to read status updates
      stompClient.subscribe(`/topic/chat/${chatId}/read`, (message) => {
        const readEvent = JSON.parse(message.body);
        // Handle read status updates
      });
      
      // Subscribe to reaction updates
      stompClient.subscribe(`/topic/chat/${chatId}/reactions`, (message) => {
        const reactionEvent = JSON.parse(message.body);
        updateMessageReaction(reactionEvent.messageId, reactionEvent.reactions);
      });
    },
    
    unsubscribeFromChat: (chatId) => {
      // This is handled automatically by STOMP when disconnecting
      // Just clean up the typing users state
      set(state => {
        const newTypingUsers = { ...state.typingUsers };
        
        Object.keys(newTypingUsers).forEach(userId => {
          if (newTypingUsers[userId].chatId === chatId) {
            clearTimeout(newTypingUsers[userId].timeout);
            delete newTypingUsers[userId];
          }
        });
        
        return { typingUsers: newTypingUsers };
      });
    },
    
    sendMessage: (chatId, messageData) => {
      if (!stompClient || !get().connected) return false;
      
      stompClient.send(
        `/app/chat/${chatId}/send`,
        {},
        JSON.stringify(messageData)
      );
      
      return true;
    },
    
    sendTypingEvent: (chatId) => {
      if (!stompClient || !get().connected) return;
      
      stompClient.send(
        `/app/chat/${chatId}/typing`,
        {},
        JSON.stringify({})
      );
    },
    
    sendReadEvent: (chatId, messageId) => {
      if (!stompClient || !get().connected) return;
      
      stompClient.send(
        `/app/chat/${chatId}/read`,
        {},
        JSON.stringify({ messageId })
      );
    }
  };
});

export default useWebSocketStore;