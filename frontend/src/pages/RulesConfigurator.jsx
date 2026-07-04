import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Switch,
  Slider,
  CircularProgress,
  Button,
  Divider,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import API from '../services/api';

const RulesConfigurator = () => {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);

  const fetchConfigs = async () => {
    try {
      const res = await API.get('/risk/configs');
      setConfigs(res.data);
    } catch (error) {
      console.error('Failed to fetch risk configurations:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchConfigs();
  }, []);

  const handleToggleModule = async (moduleKey, currentStatus) => {
    setSavingId(moduleKey);
    try {
      const res = await API.put(`/risk/configs/${moduleKey}`, {
        enabled: !currentStatus,
      });
      setConfigs((prev) =>
        prev.map((c) => (c.moduleKey === moduleKey ? res.data : c))
      );
    } catch (error) {
      console.error('Failed to toggle module:', error);
    } finally {
      setSavingId(null);
    }
  };

  const handleWeightChange = (moduleKey, newWeight) => {
    setConfigs((prev) =>
      prev.map((c) => (c.moduleKey === moduleKey ? { ...c, riskWeight: newWeight } : c))
    );
  };

  const handleSaveWeight = async (moduleKey, weight) => {
    setSavingId(moduleKey);
    try {
      const res = await API.put(`/risk/configs/${moduleKey}`, {
        riskWeight: weight,
      });
      setConfigs((prev) =>
        prev.map((c) => (c.moduleKey === moduleKey ? res.data : c))
      );
    } catch (error) {
      console.error('Failed to update weight:', error);
    } finally {
      setSavingId(null);
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
    <Box sx={{ p: 4, bgcolor: '#020617', minHeight: '90vh', color: '#fff' }}>
      <Typography variant="body1" sx={{ color: '#94a3b8', mb: 4, maxWidth: '600px' }}>
        Configure risk engine scores and enabled statuses for each detection module. Changes are applied dynamically to the gateway.
      </Typography>

      <Grid container spacing={3}>
        {configs.map((config) => (
          <Grid item xs={12} sm={6} md={4} key={config.id}>
            <Card
              sx={{
                bgcolor: '#0f172a',
                border: '1px solid rgba(255,255,255,0.06)',
                borderRadius: '16px',
                height: '100%',
                opacity: config.enabled ? 1 : 0.6,
                transition: 'opacity 0.3s',
              }}
            >
              <CardContent sx={{ display: 'flex', flexDirection: 'column', height: '90%', justifyContent: 'space-between' }}>
                <Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                    <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#fff' }}>
                      {config.moduleKey.replace(/_/g, ' ')}
                    </Typography>
                    <Switch
                      checked={config.enabled}
                      onChange={() => handleToggleModule(config.moduleKey, config.enabled)}
                      disabled={savingId === config.moduleKey}
                      color="primary"
                    />
                  </Box>
                  <Typography variant="body2" sx={{ color: '#64748b', mb: 3, minHeight: '40px' }}>
                    {config.description}
                  </Typography>
                </Box>

                <Box>
                  <Divider sx={{ borderColor: 'rgba(255,255,255,0.04)', my: 2 }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="caption" sx={{ color: '#94a3b8', fontWeight: 600 }}>Risk Weight / Penalty</Typography>
                    <Typography variant="caption" sx={{ color: '#6366f1', fontWeight: 700 }}>+{config.riskWeight}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Slider
                      value={config.riskWeight}
                      min={0}
                      max={120}
                      step={5}
                      disabled={!config.enabled || savingId === config.moduleKey}
                      onChange={(e, val) => handleWeightChange(config.moduleKey, val)}
                      sx={{ color: '#6366f1', flexGrow: 1 }}
                    />
                    <Button
                      size="small"
                      variant="contained"
                      disabled={!config.enabled || savingId === config.moduleKey}
                      onClick={() => handleSaveWeight(config.moduleKey, config.riskWeight)}
                      sx={{
                        bgcolor: '#6366f1',
                        borderRadius: '8px',
                        minWidth: '40px',
                        p: '6px',
                        '&:hover': { bgcolor: '#4f46e5' },
                      }}
                    >
                      <SaveIcon fontSize="small" />
                    </Button>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default RulesConfigurator;
