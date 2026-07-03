import React, { useState, useEffect } from 'react';
import API from '../services/api';
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
  Button,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SecurityIcon from '@mui/icons-material/Security';
import WifiTetheringIcon from '@mui/icons-material/WifiTethering';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { useNavigate } from 'react-router-dom';

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [recentLogs, setRecentLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchData = async () => {
    try {
      const statsRes = await API.get('/logs/stats');
      const logsRes = await API.get('/logs');
      setStats(statsRes.data);
      // Slice top 5 logs
      setRecentLogs(logsRes.data.slice(0, 5));
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    // Poll every 10 seconds for real-time updates
    const timer = setInterval(fetchData, 10000);
    return () => clearInterval(timer);
  }, []);

  if (loading || !stats) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress sx={{ color: '#6366f1' }} />
      </Box>
    );
  }

  // Chart configuration
  const chartData = {
    labels: stats.attacksPerDay.map((d) => d.date),
    datasets: [
      {
        label: 'Attacks Blocked',
        data: stats.attacksPerDay.map((d) => d.count),
        fill: true,
        backgroundColor: 'rgba(99, 102, 241, 0.1)',
        borderColor: '#6366f1',
        pointBackgroundColor: '#06b6d4',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: '#6366f1',
        tension: 0.35,
        borderWidth: 3,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#0f172a',
        titleFont: { family: 'Outfit', size: 13 },
        bodyFont: { family: 'Outfit', size: 13 },
        borderColor: 'rgba(255, 255, 255, 0.08)',
        borderWidth: 1,
        padding: 12,
        cornerRadius: 8,
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: '#64748b', font: { family: 'Outfit' } },
      },
      y: {
        grid: { color: 'rgba(255, 255, 255, 0.04)' },
        ticks: { color: '#64748b', font: { family: 'Outfit' } },
      },
    },
  };

  const statCards = [
    {
      title: 'Blocked Attacks',
      value: stats.blockedIpsCount,
      icon: <BlockIcon sx={{ fontSize: 36, color: '#f43f5e' }} />,
      color: '#f43f5e',
      bgGlow: 'rgba(244, 63, 94, 0.1)',
    },
    {
      title: 'Allowed Traffic',
      value: stats.allowedIpsCount,
      icon: <CheckCircleIcon sx={{ fontSize: 36, color: '#10b981' }} />,
      color: '#10b981',
      bgGlow: 'rgba(16, 185, 129, 0.1)',
    },
    {
      title: 'Blacklist Size',
      value: stats.blacklistSize,
      icon: <SecurityIcon sx={{ fontSize: 36, color: '#fb5b24' }} />,
      color: '#fb5b24',
      bgGlow: 'rgba(251, 91, 36, 0.1)',
    },
    {
      title: 'Whitelist Size',
      value: stats.whitelistSize,
      icon: <WifiTetheringIcon sx={{ fontSize: 36, color: '#06b6d4' }} />,
      color: '#06b6d4',
      bgGlow: 'rgba(6, 182, 212, 0.1)',
    },
  ];

  return (
    <Box sx={{ p: 4 }}>
      {/* Stat Grid */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statCards.map((card) => (
          <Grid item xs={12} sm={6} md={3} key={card.title}>
            <Card
              className="glass-panel"
              sx={{
                bgcolor: 'rgba(15, 23, 42, 0.4)',
                border: '1px solid rgba(255, 255, 255, 0.05)',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-4px)' },
              }}
            >
              <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 3 }}>
                <Box>
                  <Typography variant="body2" sx={{ color: '#64748b', fontWeight: 600, mb: 1 }}>
                    {card.title}
                  </Typography>
                  <Typography variant="h3" sx={{ fontWeight: 800, color: '#fff' }}>
                    {card.value}
                  </Typography>
                </Box>
                <Box
                  sx={{
                    p: 1.5,
                    borderRadius: '12px',
                    bgcolor: card.bgGlow,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  {card.icon}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Charts & Top Attacker */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Main Chart */}
        <Grid item xs={12} md={8}>
          <Card
            className="glass-panel"
            sx={{
              p: 3,
              bgcolor: 'rgba(15, 23, 42, 0.4)',
              border: '1px solid rgba(255, 255, 255, 0.05)',
              height: 380,
            }}
          >
            <Typography variant="h6" sx={{ fontWeight: 700, color: '#fff', mb: 3 }}>
              Threat Activity Timeline
            </Typography>
            <Box sx={{ height: 280 }}>
              <Line data={chartData} options={chartOptions} />
            </Box>
          </Card>
        </Grid>

        {/* Top Attacker Card */}
        <Grid item xs={12} md={4}>
          <Card
            className="glass-panel"
            sx={{
              p: 3,
              bgcolor: 'rgba(15, 23, 42, 0.4)',
              border: '1px solid rgba(255, 255, 255, 0.05)',
              height: 380,
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <Typography variant="h6" sx={{ fontWeight: 700, color: '#fff', mb: 3 }}>
              Top Security Attacker
            </Typography>

            <Box
              sx={{
                flexGrow: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: 'rgba(244, 63, 94, 0.03)',
                border: '1px solid rgba(244, 63, 94, 0.08)',
                borderRadius: '16px',
                p: 3,
                textAlign: 'center',
              }}
            >
              <BlockIcon sx={{ fontSize: 60, color: '#f43f5e', mb: 2 }} />
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#fff', mb: 1, wordBreak: 'break-all' }}>
                {stats.topAttacker?.ip || 'None'}
              </Typography>
              <Typography variant="body2" sx={{ color: '#64748b', fontWeight: 600 }}>
                Total Attacks Detected
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 800, color: '#f43f5e', mt: 1 }}>
                {stats.topAttacker?.count || 0}
              </Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Activity Log Table */}
      <Card
        className="glass-panel"
        sx={{
          p: 3,
          bgcolor: 'rgba(15, 23, 42, 0.4)',
          border: '1px solid rgba(255, 255, 255, 0.05)',
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#fff' }}>
            Recent Security Incidents
          </Typography>
          <Button
            variant="text"
            color="primary"
            endIcon={<ChevronRightIcon />}
            onClick={() => navigate('/logs')}
            sx={{ textTransform: 'none', fontWeight: 600, color: '#818cf8' }}
          >
            View All Logs
          </Button>
        </Box>

        <TableContainer component={Paper} sx={{ bgcolor: 'transparent', boxShadow: 'none' }}>
          <Table sx={{ minWidth: 650 }}>
            <TableHead>
              <TableRow sx={{ borderBottom: '2px solid rgba(255, 255, 255, 0.06)' }}>
                <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Timestamp</TableCell>
                <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>IP Address</TableCell>
                <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Event Type</TableCell>
                <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Action</TableCell>
                <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Country</TableCell>
                <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Reason</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {recentLogs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ color: '#64748b', py: 4, borderBottom: 'none' }}>
                    No security incidents logged. Gateway is silent.
                  </TableCell>
                </TableRow>
              ) : (
                recentLogs.map((log) => (
                  <TableRow
                    key={log.id}
                    sx={{
                      borderBottom: '1px solid rgba(255, 255, 255, 0.04)',
                      '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.01)' },
                    }}
                  >
                    <TableCell sx={{ color: '#e2e8f0', borderBottom: 'none' }}>
                      {new Date(log.timestamp).toLocaleString()}
                    </TableCell>
                    <TableCell sx={{ color: '#fff', fontWeight: 600, borderBottom: 'none' }}>{log.ip}</TableCell>
                    <TableCell sx={{ borderBottom: 'none' }}>
                      <span
                        style={{
                          padding: '4px 8px',
                          borderRadius: '6px',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          backgroundColor:
                            log.eventType === 'DDOS'
                              ? 'rgba(251, 91, 36, 0.1)'
                              : log.eventType === 'BRUTE_FORCE'
                              ? 'rgba(244, 63, 94, 0.1)'
                              : 'rgba(99, 102, 241, 0.1)',
                          color:
                            log.eventType === 'DDOS'
                              ? '#fb5b24'
                              : log.eventType === 'BRUTE_FORCE'
                              ? '#f43f5e'
                              : '#818cf8',
                        }}
                      >
                        {log.eventType}
                      </span>
                    </TableCell>
                    <TableCell sx={{ borderBottom: 'none' }}>
                      <span
                        style={{
                          fontWeight: 700,
                          color: log.actionTaken === 'BLOCKED' ? '#f43f5e' : '#10b981',
                        }}
                      >
                        {log.actionTaken}
                      </span>
                    </TableCell>
                    <TableCell sx={{ color: '#e2e8f0', borderBottom: 'none' }}>{log.country}</TableCell>
                    <TableCell sx={{ color: '#94a3b8', borderBottom: 'none' }}>{log.reason}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </Box>
  );
};

export default Dashboard;
