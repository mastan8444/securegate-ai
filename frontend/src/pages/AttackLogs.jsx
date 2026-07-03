import React, { useState, useEffect } from 'react';
import API from '../services/api';
import {
  Box,
  Card,
  Typography,
  TextField,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import RefreshIcon from '@mui/icons-material/Refresh';

const AttackLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [eventTypeFilter, setEventTypeFilter] = useState('ALL');
  const [actionFilter, setActionFilter] = useState('ALL');

  const fetchLogs = async () => {
    try {
      setLoading(true);
      const response = await API.get('/logs');
      setLogs(response.data);
    } catch (error) {
      console.error('Error fetching logs:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const filteredLogs = logs.filter((log) => {
    const matchesSearch =
      log.ip.includes(searchQuery) ||
      log.reason.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.country.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesType = eventTypeFilter === 'ALL' || log.eventType === eventTypeFilter;
    const matchesAction = actionFilter === 'ALL' || log.actionTaken === actionFilter;

    return matchesSearch && matchesType && matchesAction;
  });

  return (
    <Box sx={{ p: 4 }}>
      {/* Filters Toolbar */}
      <Box
        sx={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: 2.5,
          alignItems: 'center',
          mb: 4,
          justifyContent: 'space-between',
        }}
      >
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2.5, alignItems: 'center' }}>
          {/* Search */}
          <TextField
            placeholder="Search logs by IP, Country or Reason..."
            variant="outlined"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ color: '#475569', mr: 1 }} />,
            }}
            sx={{
              width: 320,
              '& .MuiOutlinedInput-root': {
                color: '#fff',
                bgcolor: 'rgba(255, 255, 255, 0.02)',
                borderRadius: '12px',
                '& fieldset': { borderColor: 'rgba(255, 255, 255, 0.08)' },
                '&:hover fieldset': { borderColor: 'rgba(255, 255, 255, 0.15)' },
                '&.Mui-focused fieldset': { borderColor: '#6366f1' },
              },
            }}
          />

          {/* Event Type Filter */}
          <FormControl
            sx={{
              minWidth: 150,
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
          >
            <InputLabel>Event Type</InputLabel>
            <Select
              value={eventTypeFilter}
              label="Event Type"
              onChange={(e) => setEventTypeFilter(e.target.value)}
            >
              <MenuItem value="ALL">All Events</MenuItem>
              <MenuItem value="BRUTE_FORCE">Brute Force</MenuItem>
              <MenuItem value="DDOS">DDoS Attacks</MenuItem>
              <MenuItem value="GEOLOCATION_VIOLATION">Country Block</MenuItem>
              <MenuItem value="CIDR_VIOLATION">CIDR Block</MenuItem>
            </Select>
          </FormControl>

          {/* Action Filter */}
          <FormControl
            sx={{
              minWidth: 150,
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
          >
            <InputLabel>Action</InputLabel>
            <Select
              value={actionFilter}
              label="Action"
              onChange={(e) => setActionFilter(e.target.value)}
            >
              <MenuItem value="ALL">All Actions</MenuItem>
              <MenuItem value="BLOCKED">Blocked</MenuItem>
              <MenuItem value="ALLOWED">Allowed</MenuItem>
            </Select>
          </FormControl>
        </Box>

        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={fetchLogs}
          sx={{
            borderColor: 'rgba(255, 255, 255, 0.08)',
            color: '#94a3b8',
            borderRadius: '12px',
            textTransform: 'none',
            fontWeight: 600,
            '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.02)', borderColor: 'rgba(255, 255, 255, 0.2)' },
          }}
        >
          Refresh Logs
        </Button>
      </Box>

      {/* Main Table */}
      <Card
        className="glass-panel"
        sx={{
          p: 3,
          bgcolor: 'rgba(15, 23, 42, 0.4)',
          border: '1px solid rgba(255, 255, 255, 0.05)',
        }}
      >
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress sx={{ color: '#6366f1' }} />
          </Box>
        ) : (
          <TableContainer component={Paper} sx={{ bgcolor: 'transparent', boxShadow: 'none' }}>
            <Table sx={{ minWidth: 650 }}>
              <TableHead>
                <TableRow sx={{ borderBottom: '2px solid rgba(255, 255, 255, 0.06)' }}>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Timestamp</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>IP Address</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Event Type</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Action Taken</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Country</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Reason / Alert Details</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredLogs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ color: '#64748b', py: 4, borderBottom: 'none' }}>
                      No logs found matching selected criteria.
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredLogs.map((log) => (
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
                                : log.eventType === 'GEOLOCATION_VIOLATION'
                                ? 'rgba(168, 85, 247, 0.1)'
                                : 'rgba(99, 102, 241, 0.1)',
                            color:
                              log.eventType === 'DDOS'
                                ? '#fb5b24'
                                : log.eventType === 'BRUTE_FORCE'
                                ? '#f43f5e'
                                : log.eventType === 'GEOLOCATION_VIOLATION'
                                ? '#a855f7'
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
        )}
      </Card>
    </Box>
  );
};

export default AttackLogs;
