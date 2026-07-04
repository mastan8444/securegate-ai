import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  CircularProgress,
  Chip,
} from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningIcon from '@mui/icons-material/Warning';
import API from '../services/api';

const RiskEngineDashboard = () => {
  const [metrics, setMetrics] = useState(null);
  const [assessments, setAssessments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDashboardData = async () => {
    try {
      const [metricsRes, assessmentsRes] = await Promise.all([
        API.get('/risk/metrics'),
        API.get('/risk/assessments'),
      ]);
      setMetrics(metricsRes.data);
      setAssessments(assessmentsRes.data);
    } catch (error) {
      console.error('Failed to fetch risk engine metrics:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 10000); // Auto-refresh every 10s
    return () => clearInterval(interval);
  }, []);

  const getScoreColor = (score) => {
    if (score >= 120) return '#ef4444'; // Red
    if (score >= 90) return '#f97316'; // Orange
    if (score >= 60) return '#eab308'; // Yellow
    if (score >= 30) return '#3b82f6'; // Blue
    return '#10b981'; // Green
  };

  const getActionChipColor = (action) => {
    switch (action) {
      case 'BLOCK':
        return 'error';
      case 'MFA':
        return 'warning';
      case 'CAPTCHA':
        return 'primary';
      case 'LOG':
        return 'info';
      default:
        return 'success';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress sx={{ color: '#6366f1' }} />
      </Box>
    );
  }

  const distribution = metrics?.actionDistribution || {};

  return (
    <Box sx={{ p: 4, bgcolor: '#020617', minHeight: '90vh', color: '#fff' }}>
      {/* Metrics Row */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3.2}>
          <Card sx={{ bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '16px' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', justifyItems: 'space-between', gap: 2 }}>
              <Box sx={{ p: 1.5, borderRadius: '12px', bgcolor: 'rgba(99, 102, 241, 0.1)', color: '#6366f1' }}>
                <ShieldIcon fontSize="large" />
              </Box>
              <Box>
                <Typography variant="body2" sx={{ color: '#94a3b8', fontWeight: 600 }}>Total Evaluations</Typography>
                <Typography variant="h4" sx={{ fontWeight: 800, color: '#fff' }}>{metrics?.totalAssessments || 0}</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={2.2}>
          <Card sx={{ bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '16px' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Box sx={{ p: 1, borderRadius: '8px', bgcolor: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                <CheckCircleIcon />
              </Box>
              <Box>
                <Typography variant="caption" sx={{ color: '#94a3b8', fontWeight: 600 }}>Allowed (ALLOW/LOG)</Typography>
                <Typography variant="h6" sx={{ fontWeight: 800, color: '#fff' }}>
                  {(distribution.ALLOW || 0) + (distribution.LOG || 0)}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={2.2}>
          <Card sx={{ bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '16px' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Box sx={{ p: 1, borderRadius: '8px', bgcolor: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }}>
                <BlockIcon />
              </Box>
              <Box>
                <Typography variant="caption" sx={{ color: '#94a3b8', fontWeight: 600 }}>Blocked Sessions</Typography>
                <Typography variant="h6" sx={{ fontWeight: 800, color: '#fff' }}>{distribution.BLOCK || 0}</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={2.2}>
          <Card sx={{ bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '16px' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Box sx={{ p: 1, borderRadius: '8px', bgcolor: 'rgba(234, 179, 8, 0.1)', color: '#eab308' }}>
                <WarningIcon />
              </Box>
              <Box>
                <Typography variant="caption" sx={{ color: '#94a3b8', fontWeight: 600 }}>Challenged (CAPTCHA/MFA)</Typography>
                <Typography variant="h6" sx={{ fontWeight: 800, color: '#fff' }}>
                  {(distribution.CAPTCHA || 0) + (distribution.MFA || 0)}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Main Grid: Heatmaps, Top Attacks, Audit Timeline */}
      <Grid container spacing={3}>
        {/* Left Side: Top Attacks */}
        <Grid item xs={12} md={4}>
          <Card sx={{ bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '16px', height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 3, color: '#fff' }}>Top Threat Indicators</Typography>
              {Object.keys(metrics?.topAttacks || {}).length === 0 ? (
                <Typography variant="body2" sx={{ color: '#64748b' }}>No threat anomalies recorded yet.</Typography>
              ) : (
                Object.entries(metrics.topAttacks).map(([type, count]) => (
                  <Box key={type} sx={{ display: 'flex', justifyContent: 'space-between', mb: 2, pb: 1, borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                    <Typography variant="body2" sx={{ color: '#94a3b8', fontWeight: 500 }}>{type}</Typography>
                    <Typography variant="body2" sx={{ color: '#6366f1', fontWeight: 700 }}>{count} hits</Typography>
                  </Box>
                ))
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Right Side: Risk Assessment Timeline */}
        <Grid item xs={12} md={8}>
          <Card sx={{ bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '16px' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 3, color: '#fff' }}>Risk Audit Logs (Real-time)</Typography>
              <TableContainer component={Paper} sx={{ bgcolor: 'transparent', boxShadow: 'none' }}>
                <Table sx={{ minWidth: 650 }}>
                  <TableHead>
                    <TableRow sx={{ borderBottom: '2px solid rgba(255,255,255,0.08)' }}>
                      <TableCell sx={{ color: '#64748b', fontWeight: 700 }}>Client IP</TableCell>
                      <TableCell sx={{ color: '#64748b', fontWeight: 700 }}>Risk Score</TableCell>
                      <TableCell sx={{ color: '#64748b', fontWeight: 700 }}>Action</TableCell>
                      <TableCell sx={{ color: '#64748b', fontWeight: 700 }}>Indicators / Reason</TableCell>
                      <TableCell sx={{ color: '#64748b', fontWeight: 700 }}>Timestamp</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {assessments.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={5} align="center" sx={{ color: '#64748b', py: 4 }}>
                          No risk logs recorded yet.
                        </TableCell>
                      </TableRow>
                    ) : (
                      assessments.map((ra) => (
                        <TableRow key={ra.id} sx={{ '&:hover': { bgcolor: 'rgba(255,255,255,0.02)' }, borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                          <TableCell sx={{ color: '#fff', fontWeight: 600, fontFamily: 'monospace' }}>{ra.ipAddress}</TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: getScoreColor(ra.riskScore) }} />
                              <Typography sx={{ fontWeight: 700, color: getScoreColor(ra.riskScore) }}>{ra.riskScore}</Typography>
                            </Box>
                          </TableCell>
                          <TableCell>
                            <Chip label={ra.actionTaken} color={getActionChipColor(ra.actionTaken)} size="small" sx={{ fontWeight: 700, borderRadius: '6px' }} />
                          </TableCell>
                          <TableCell sx={{ color: '#94a3b8', fontSize: '0.85rem' }}>{ra.reason}</TableCell>
                          <TableCell sx={{ color: '#64748b' }}>{new Date(ra.createdAt).toLocaleString()}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default RiskEngineDashboard;
