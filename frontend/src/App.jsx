import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline, Box } from '@mui/material';
import { AuthProvider, useAuth } from './context/AuthContext';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Blacklist from './pages/Blacklist';
import Whitelist from './pages/Whitelist';
import AttackLogs from './pages/AttackLogs';
import RulesManager from './pages/RulesManager';
import RiskEngineDashboard from './pages/RiskEngineDashboard';
import RulesConfigurator from './pages/RulesConfigurator';

// Material UI Dark Theme Config
const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#6366f1' }, // Indigo
    secondary: { main: '#06b6d4' }, // Cyan
    background: {
      default: '#080c14',
      paper: '#0f172a',
    },
    text: {
      primary: '#f8fafc',
      secondary: '#94a3b8',
    },
  },
  typography: {
    fontFamily: "'Outfit', 'Roboto', 'Helvetica', 'Arial', sans-serif",
    h5: { fontWeight: 700 },
    h6: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: '12px',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: '16px',
        },
      },
    },
  },
});

// Protect routes that need auth
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) return null; // Avoid redirecting while reading token from local storage
  if (!user) return <Navigate to="/login" replace />;

  return (
    <Box sx={{ display: 'flex' }}>
      <Sidebar />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minHeight: '100vh',
          bgcolor: 'background.default',
          pt: '70px', // Shift content below the header
          pl: '260px', // Shift content right of the sidebar
        }}
      >
        <Header />
        {children}
      </Box>
    </Box>
  );
};

// Protect routes that should not be accessible if logged in
const PublicRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) return null;
  if (user) return <Navigate to="/" replace />;

  return children;
};

function App() {
  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            {/* Public Auth Endpoint */}
            <Route
              path="/login"
              element={
                <PublicRoute>
                  <Login />
                </PublicRoute>
              }
            />

            {/* Protected Dashboard Admin Endpoints */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/blacklist"
              element={
                <ProtectedRoute>
                  <Blacklist />
                </ProtectedRoute>
              }
            />
            <Route
              path="/whitelist"
              element={
                <ProtectedRoute>
                  <Whitelist />
                </ProtectedRoute>
              }
            />
            <Route
              path="/logs"
              element={
                <ProtectedRoute>
                  <AttackLogs />
                </ProtectedRoute>
              }
            />
            <Route
              path="/settings"
              element={
                <ProtectedRoute>
                  <RulesManager />
                </ProtectedRoute>
              }
            />
            <Route
              path="/risk-engine"
              element={
                <ProtectedRoute>
                  <RiskEngineDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/waf-rules"
              element={
                <ProtectedRoute>
                  <RulesConfigurator />
                </ProtectedRoute>
              }
            />

            {/* Fallback Catch-all */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;

