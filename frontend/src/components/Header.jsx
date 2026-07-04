import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Box, Typography } from '@mui/material';

const Header = () => {
  const location = useLocation();
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const getPageTitle = () => {
    switch (location.pathname) {
      case '/':
        return 'Dashboard Analytics';
      case '/risk-engine':
        return 'Enterprise Risk Analysis Engine';
      case '/waf-rules':
        return 'Web Application Firewall (WAF) Rules';
      case '/blacklist':
        return 'IP Blacklist';
      case '/whitelist':
        return 'IP Whitelist';
      case '/logs':
        return 'Security Attack Logs';
      case '/settings':
        return 'Security Policy Configuration';
      default:
        return 'SecureGate AI';
    }
  };

  return (
    <Box
      sx={{
        height: 70,
        bgcolor: '#0f172a',
        borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        px: 4,
        position: 'fixed',
        top: 0,
        right: 0,
        left: 260, // Align right of Sidebar
        zIndex: 100,
      }}
    >
      <Typography variant="h5" sx={{ fontWeight: 700, color: '#fff' }}>
        {getPageTitle()}
      </Typography>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
        {/* Tenant Scope Badge */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            bgcolor: 'rgba(99, 102, 241, 0.08)',
            border: '1px solid rgba(99, 102, 241, 0.2)',
            borderRadius: '20px',
            px: 2,
            py: 0.5,
          }}
        >
          <Typography variant="caption" sx={{ color: '#6366f1', fontWeight: 700, letterSpacing: 0.3, textTransform: 'uppercase' }}>
            TENANT: {localStorage.getItem('tenantId') || 'system'}
          </Typography>
        </Box>

        {/* Status Indicator */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            bgcolor: 'rgba(16, 185, 129, 0.08)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
            borderRadius: '20px',
            px: 2,
            py: 0.5,
          }}
        >
          <span className="pulsing-dot"></span>
          <Typography variant="caption" sx={{ color: '#10b981', fontWeight: 600, letterSpacing: 0.3 }}>
            GATEWAY ACTIVE
          </Typography>
        </Box>

        {/* DateTime Display */}
        <Typography variant="body2" sx={{ color: '#64748b', fontWeight: 500 }}>
          {time.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            year: 'numeric',
          })}{' '}
          • {time.toLocaleTimeString('en-US', { hour12: false })}
        </Typography>
      </Box>
    </Box>
  );
};

export default Header;
