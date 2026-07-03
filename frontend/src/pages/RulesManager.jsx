import React, { useState, useEffect } from 'react';
import API from '../services/api';
import {
  Box,
  Card,
  Grid,
  Typography,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Divider,
  CircularProgress,
  Alert,
} from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import ScienceIcon from '@mui/icons-material/Science';

const RulesManager = () => {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [alertMsg, setAlertMsg] = useState({ type: '', text: '' });

  // Simulator Fields
  const [simIp, setSimIp] = useState('192.168.1.99');
  const [simResult, setSimResult] = useState(null);
  const [simLoading, setSimLoading] = useState(false);

  const fetchRules = async () => {
    try {
      const response = await API.get('/rules');
      setRules(response.data);
    } catch (error) {
      console.error('Error fetching rules:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleRuleToggle = async (key, currentValue, currentEnabled) => {
    try {
      const updated = await API.put(`/rules/${key}`, {
        ruleValue: currentValue,
        enabled: !currentEnabled,
      });
      
      setRules(rules.map((r) => (r.ruleKey === key ? updated.data : r)));
      setAlertMsg({ type: 'success', text: `Rule ${key} updated successfully!` });
    } catch (error) {
      setAlertMsg({ type: 'error', text: 'Failed to update rule' });
    }
  };

  const handleRuleValueChange = async (key, newValue, currentEnabled) => {
    try {
      const updated = await API.put(`/rules/${key}`, {
        ruleValue: newValue,
        enabled: currentEnabled,
      });
      
      setRules(rules.map((r) => (r.ruleKey === key ? updated.data : r)));
      setAlertMsg({ type: 'success', text: `Rule value updated to "${newValue}"` });
    } catch (error) {
      setAlertMsg({ type: 'error', text: 'Failed to update rule value' });
    }
  };

  const runSimulation = async (type) => {
    if (!simIp) {
      alert('Please provide an IP address for simulation');
      return;
    }

    setSimLoading(true);
    setSimResult(null);

    try {
      let response;
      if (type === 'failed-login') {
        response = await API.post('/simulate/failed-login', { ip: simIp });
        setSimResult({
          title: 'Failed Login Simulation',
          message: response.data.message,
          ip: response.data.ip,
          status: response.data.currentStatus,
          reason: response.data.reason || 'None',
        });
      } else if (type === 'ddos') {
        response = await API.post('/simulate/ddos', { ip: simIp });
        setSimResult({
          title: 'DDoS Burst Simulation',
          message: `Simulated ${response.data.requestsSimulated} HTTP requests in 1 second.`,
          ip: response.data.ip,
          status: response.data.currentStatus,
          reason: response.data.reason || 'None',
        });
      } else if (type === 'check') {
        response = await API.get(`/check/${simIp}`);
        setSimResult({
          title: 'Access Gateway Check',
          message: response.data.allowed ? 'IP is ALLOWED access' : 'IP is BLOCKED access',
          ip: simIp,
          status: response.data.status,
          reason: response.data.reason,
        });
      }
    } catch (error) {
      // Axios throws on 403 Forbidden which check returns when blocked
      if (error.response?.status === 403) {
        setSimResult({
          title: 'Access Gateway Check',
          message: 'IP is BLOCKED access',
          ip: simIp,
          status: error.response.data?.status || 'BLOCKED',
          reason: error.response.data?.reason || 'Blacklisted or Country block',
        });
      } else {
        alert('Simulation failed: ' + (error.response?.data || error.message));
      }
    } finally {
      setSimLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress sx={{ color: '#6366f1' }} />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 4 }}>
      {alertMsg.text && (
        <Alert
          severity={alertMsg.type}
          onClose={() => setAlertMsg({ type: '', text: '' })}
          sx={{ mb: 4, borderRadius: '12px' }}
        >
          {alertMsg.text}
        </Alert>
      )}

      <Grid container spacing={4}>
        {/* Rules Editor */}
        <Grid item xs={12} md={7}>
          <Card
            className="glass-panel"
            sx={{
              p: 4,
              bgcolor: 'rgba(15, 23, 42, 0.4)',
              border: '1px solid rgba(255, 255, 255, 0.05)',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
              <SettingsIcon sx={{ color: '#6366f1', fontSize: 28 }} />
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#fff' }}>
                Active Protection Rules
              </Typography>
            </Box>

            {rules.map((rule, idx) => (
              <Box key={rule.ruleKey} sx={{ mb: 3 }}>
                <Grid container spacing={2} alignItems="center">
                  <Grid item xs={12} sm={5}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#fff' }}>
                      {rule.ruleKey}
                    </Typography>
                    <Typography variant="caption" sx={{ color: '#64748b' }}>
                      {rule.description}
                    </Typography>
                  </Grid>

                  <Grid item xs={8} sm={5}>
                    <TextField
                      fullWidth
                      size="small"
                      defaultValue={rule.ruleValue}
                      onBlur={(e) => {
                        if (e.target.value !== rule.ruleValue) {
                          handleRuleValueChange(rule.ruleKey, e.target.value, rule.isEnabled());
                        }
                      }}
                      sx={{
                        '& .MuiOutlinedInput-root': {
                          color: '#fff',
                          borderRadius: '10px',
                          '& fieldset': { borderColor: 'rgba(255, 255, 255, 0.08)' },
                          '&:hover fieldset': { borderColor: 'rgba(255, 255, 255, 0.15)' },
                          '&.Mui-focused fieldset': { borderColor: '#6366f1' },
                        },
                      }}
                    />
                  </Grid>

                  <Grid item xs={4} sm={2} sx={{ textAlign: 'right' }}>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={rule.enabled}
                          onChange={() => handleRuleToggle(rule.ruleKey, rule.ruleValue, rule.isEnabled())}
                          color="primary"
                        />
                      }
                      label=""
                      sx={{ mr: 0 }}
                    />
                  </Grid>
                </Grid>
                {idx < rules.length - 1 && <Divider sx={{ my: 2, borderColor: 'rgba(255, 255, 255, 0.06)' }} />}
              </Box>
            ))}
          </Card>
        </Grid>

        {/* Security Simulator */}
        <Grid item xs={12} md={5}>
          <Card
            className="glass-panel"
            sx={{
              p: 4,
              bgcolor: 'rgba(15, 23, 42, 0.4)',
              border: '1px solid rgba(255, 255, 255, 0.05)',
              height: '100%',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
              <ScienceIcon sx={{ color: '#06b6d4', fontSize: 28 }} />
              <Typography variant="h6" sx={{ fontWeight: 700, color: '#fff' }}>
                Rule Testing & Simulation Lab
              </Typography>
            </Box>

            <Typography variant="body2" sx={{ color: '#94a3b8', mb: 3 }}>
              Verify your firewall policies immediately by simulating threats (Brute Force login failures or DDoS requests) from a specific IP.
            </Typography>

            <TextField
              fullWidth
              label="Simulator Target IP"
              value={simIp}
              onChange={(e) => setSimIp(e.target.value)}
              placeholder="e.g. 192.168.1.100"
              sx={{
                mb: 3,
                '& label': { color: '#64748b' },
                '& label.Mui-focused': { color: '#6366f1' },
                '& .MuiOutlinedInput-root': {
                  color: '#fff',
                  borderRadius: '12px',
                  '& fieldset': { borderColor: 'rgba(255, 255, 255, 0.08)' },
                  '&:hover fieldset': { borderColor: 'rgba(255, 255, 255, 0.15)' },
                  '&.Mui-focused fieldset': { borderColor: '#6366f1' },
                },
              }}
            />

            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12}>
                <Button
                  fullWidth
                  variant="outlined"
                  onClick={() => runSimulation('failed-login')}
                  disabled={simLoading}
                  sx={{
                    borderColor: 'rgba(244, 63, 94, 0.2)',
                    color: '#f43f5e',
                    borderRadius: '10px',
                    textTransform: 'none',
                    fontWeight: 600,
                    '&:hover': { bgcolor: 'rgba(244, 63, 94, 0.06)', borderColor: '#f43f5e' },
                  }}
                >
                  Simulate Failed Login
                </Button>
              </Grid>

              <Grid item xs={6}>
                <Button
                  fullWidth
                  variant="outlined"
                  onClick={() => runSimulation('ddos')}
                  disabled={simLoading}
                  sx={{
                    borderColor: 'rgba(251, 91, 36, 0.2)',
                    color: '#fb5b24',
                    borderRadius: '10px',
                    textTransform: 'none',
                    fontWeight: 600,
                    '&:hover': { bgcolor: 'rgba(251, 91, 36, 0.06)', borderColor: '#fb5b24' },
                  }}
                >
                  Simulate DDoS Burst
                </Button>
              </Grid>

              <Grid item xs={6}>
                <Button
                  fullWidth
                  variant="contained"
                  onClick={() => runSimulation('check')}
                  disabled={simLoading}
                  sx={{
                    bgcolor: '#6366f1',
                    borderRadius: '10px',
                    textTransform: 'none',
                    fontWeight: 600,
                    '&:hover': { bgcolor: '#4f46e5' },
                  }}
                >
                  Check Gateway Access
                </Button>
              </Grid>
            </Grid>

            {/* Simulation Results Output */}
            <Box
              sx={{
                flexGrow: 1,
                bgcolor: '#090d16',
                border: '1px solid rgba(255, 255, 255, 0.04)',
                borderRadius: '12px',
                p: 3,
                display: 'flex',
                flexDirection: 'column',
                justifyContent: simResult ? 'flex-start' : 'center',
                alignItems: simResult ? 'flex-start' : 'center',
              }}
            >
              {simLoading ? (
                <CircularProgress size={30} sx={{ color: '#6366f1' }} />
              ) : simResult ? (
                <Box sx={{ width: '100%' }}>
                  <Typography variant="subtitle2" sx={{ color: '#06b6d4', fontWeight: 700, mb: 1 }}>
                    {simResult.title}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#fff', mb: 2 }}>
                    {simResult.message}
                  </Typography>

                  <Grid container spacing={1}>
                    <Grid item xs={4}>
                      <Typography variant="caption" sx={{ color: '#64748b' }}>Target IP:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Typography variant="body2" sx={{ color: '#fff', fontWeight: 600 }}>{simResult.ip}</Typography>
                    </Grid>

                    <Grid item xs={4}>
                      <Typography variant="caption" sx={{ color: '#64748b' }}>Result Status:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <span
                        style={{
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          padding: '2px 6px',
                          borderRadius: '4px',
                          backgroundColor: simResult.status === 'ALLOWED' ? 'rgba(16, 185, 129, 0.1)' : 'rgba(244, 63, 94, 0.1)',
                          color: simResult.status === 'ALLOWED' ? '#10b981' : '#f43f5e',
                        }}
                      >
                        {simResult.status}
                      </span>
                    </Grid>

                    <Grid item xs={4}>
                      <Typography variant="caption" sx={{ color: '#64748b' }}>Block Reason:</Typography>
                    </Grid>
                    <Grid item xs={8}>
                      <Typography variant="caption" sx={{ color: '#94a3b8' }}>{simResult.reason}</Typography>
                    </Grid>
                  </Grid>
                </Box>
              ) : (
                <Typography variant="caption" sx={{ color: '#475569' }}>
                  Awaiting simulation trigger...
                </Typography>
              )}
            </Box>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default RulesManager;
