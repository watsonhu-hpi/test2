import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { 
  Box, 
  List, 
  ListItem, 
  ListItemButton, 
  ListItemIcon, 
  ListItemText,
  Divider, 
  Typography,
  TextField,
  InputAdornment,
  IconButton
} from '@mui/material';
import {
  Chat as ChatIcon,
  Person as ContactIcon,
  Settings as SettingsIcon,
  Add as AddIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import ChatList from '../chat/ChatList';
import CreateChatDialog from '../chat/CreateChatDialog';
import useChatStore from '../../stores/chatStore';

const Sidebar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = React.useState('');
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const { fetchChats, chats } = useChatStore();
  
  React.useEffect(() => {
    fetchChats();
  }, [fetchChats]);
  
  const handleCreateChat = () => {
    setDialogOpen(true);
  };
  
  const filteredChats = React.useMemo(() => {
    if (!searchTerm) return chats;
    
    return chats.filter(chat => 
      chat.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [chats, searchTerm]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2 }}>
        <TextField
          size="small"
          placeholder="Search chats..."
          fullWidth
          variant="outlined"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            )
          }}
        />
      </Box>
      
      <List>
        <ListItem disablePadding>
          <ListItemButton 
            selected={location.pathname === '/' || location.pathname.startsWith('/chat')}
            component={Link} 
            to="/"
          >
            <ListItemIcon>
              <ChatIcon />
            </ListItemIcon>
            <ListItemText primary="Chats" />
          </ListItemButton>
        </ListItem>
        
        <ListItem disablePadding>
          <ListItemButton 
            selected={location.pathname === '/contacts'}
            component={Link} 
            to="/contacts"
          >
            <ListItemIcon>
              <ContactIcon />
            </ListItemIcon>
            <ListItemText primary="Contacts" />
          </ListItemButton>
        </ListItem>
        
        <ListItem disablePadding>
          <ListItemButton 
            selected={location.pathname === '/settings'}
            component={Link} 
            to="/settings"
          >
            <ListItemIcon>
              <SettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItemButton>
        </ListItem>
      </List>
      
      <Divider />
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', px: 2, py: 1 }}>
        <Typography variant="subtitle1">Recent Chats</Typography>
        <IconButton size="small" onClick={handleCreateChat}>
          <AddIcon />
        </IconButton>
      </Box>
      
      <Box sx={{ flexGrow: 1, overflow: 'auto' }}>
        <ChatList chats={filteredChats} />
      </Box>
      
      <CreateChatDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
      />
    </Box>
  );
};

export default Sidebar;