import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Divider,
  Button,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SecurityIcon from '@mui/icons-material/Security';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import ShieldIcon from '@mui/icons-material/Shield';
import SpeedIcon from '@mui/icons-material/Speed';
import RuleIcon from '@mui/icons-material/Rule';

const Sidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
    { text: 'Risk Engine', icon: <SpeedIcon />, path: '/risk-engine' },
    { text: 'WAF Rules', icon: <RuleIcon />, path: '/waf-rules' },
    { text: 'Blacklist IP', icon: <BlockIcon />, path: '/blacklist' },
    { text: 'Whitelist IP', icon: <CheckCircleIcon />, path: '/whitelist' },
    { text: 'Attack Logs', icon: <SecurityIcon />, path: '/logs' },
    { text: 'Security Settings', icon: <SettingsIcon />, path: '/settings' },
  ];

  return (
    <Box
      sx={{
        width: 260,
        height: '100vh',
        bgcolor: '#0f172a',
        borderRight: '1px solid rgba(255, 255, 255, 0.06)',
        display: 'flex',
        flexDirection: 'column',
        position: 'fixed',
        top: 0,
        left: 0,
      }}
    >
      {/* Platform Title */}
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <ShieldIcon sx={{ color: '#6366f1', fontSize: 32 }} />
        <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: 0.5, color: '#fff' }}>
          SecureGate <span style={{ color: '#06b6d4' }}>AI</span>
        </Typography>
      </Box>

      <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.06)' }} />

      {/* Navigation List */}
      <List sx={{ px: 2, py: 3, flexGrow: 1 }}>
        {menuItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
              <ListItemButton
                onClick={() => navigate(item.path)}
                sx={{
                  borderRadius: '12px',
                  bgcolor: isActive ? 'rgba(99, 102, 241, 0.15)' : 'transparent',
                  color: isActive ? '#818cf8' : '#94a3b8',
                  border: isActive ? '1px solid rgba(99, 102, 241, 0.25)' : '1px solid transparent',
                  '&:hover': {
                    bgcolor: 'rgba(255, 255, 255, 0.03)',
                    color: '#fff',
                    '& .MuiListItemIcon-root': { color: '#fff' },
                  },
                  transition: 'all 0.2s',
                }}
              >
                <ListItemIcon
                  sx={{
                    color: isActive ? '#818cf8' : '#64748b',
                    minWidth: 40,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontSize: '0.95rem',
                    fontWeight: isActive ? 600 : 500,
                  }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>

      {/* Footer Profile Box */}
      <Box sx={{ p: 2, borderTop: '1px solid rgba(255, 255, 255, 0.06)', bgcolor: '#0b0f19' }}>
        <Box sx={{ mb: 2, display: 'flex', flexDirection: 'column' }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#fff' }}>
            {user?.username || 'Administrator'}
          </Typography>
          <Typography variant="caption" sx={{ color: '#64748b' }}>
            {user?.role === 'ROLE_ADMIN' ? 'Security Admin' : 'Staff'}
          </Typography>
        </Box>
        <Button
          fullWidth
          variant="outlined"
          color="error"
          startIcon={<LogoutIcon />}
          onClick={logout}
          sx={{
            borderRadius: '10px',
            borderColor: 'rgba(244, 63, 94, 0.3)',
            color: '#f43f5e',
            '&:hover': {
              bgcolor: 'rgba(244, 63, 94, 0.06)',
              borderColor: '#f43f5e',
            },
            textTransform: 'none',
            fontWeight: 600,
          }}
        >
          Logout
        </Button>
      </Box>
    </Box>
  );
};

export default Sidebar;
