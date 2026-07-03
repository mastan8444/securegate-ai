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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import DownloadIcon from '@mui/icons-material/Download';
import UploadIcon from '@mui/icons-material/Upload';
import SearchIcon from '@mui/icons-material/Search';

const Blacklist = () => {
  const [blacklist, setBlacklist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [openAddDialog, setOpenAddDialog] = useState(false);
  
  // Form fields
  const [ipAddress, setIpAddress] = useState('');
  const [reason, setReason] = useState('');
  const [status, setStatus] = useState('PERMANENT');
  const [durationHours, setDurationHours] = useState('24');
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');

  const fetchBlacklist = async () => {
    try {
      const response = await API.get('/blacklist');
      setBlacklist(response.data);
    } catch (error) {
      console.error('Error fetching blacklist:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBlacklist();
  }, []);

  const handleOpenDialog = () => {
    setIpAddress('');
    setReason('');
    setStatus('PERMANENT');
    setDurationHours('24');
    setFormError('');
    setFormSuccess('');
    setOpenAddDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenAddDialog(false);
  };

  const handleAddIp = async (e) => {
    e.preventDefault();
    if (!ipAddress) {
      setFormError('IP Address is required');
      return;
    }

    try {
      await API.post('/blacklist', {
        ipAddress,
        reason,
        status,
        durationHours: status === 'TEMPORARY' ? durationHours : null,
      });
      setFormSuccess('IP blacklisted successfully!');
      fetchBlacklist();
      setTimeout(handleCloseDialog, 1000);
    } catch (error) {
      setFormError(error.response?.data?.message || 'Failed to block IP');
    }
  };

  const handleUnblockIp = async (ip) => {
    if (window.confirm(`Are you sure you want to unblock IP ${ip}?`)) {
      try {
        await API.delete(`/blacklist/${ip}`);
        fetchBlacklist();
      } catch (error) {
        console.error('Failed to unblock IP:', error);
      }
    }
  };

  const handleExportCsv = () => {
    // Navigate browser to download endpoint directly, adding token as query param isn't needed if backend supports direct CSV download,
    // but since we need Authorization header, we can fetch it via Axios and trigger a browser download programmatically!
    API.get('/export/blacklist', { responseType: 'blob' })
      .then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'securegate_blacklist.csv');
        document.body.appendChild(link);
        link.click();
        link.remove();
      })
      .catch((error) => console.error('Failed to export CSV:', error));
  };

  const handleImportCsv = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
      setLoading(true);
      await API.post('/import/blacklist', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      alert('CSV blacklist imported successfully!');
      fetchBlacklist();
    } catch (error) {
      alert('Failed to import CSV: ' + (error.response?.data || error.message));
    } finally {
      setLoading(false);
    }
  };

  const filteredList = blacklist.filter(
    (item) =>
      item.ipAddress.includes(searchQuery) ||
      item.reason.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <Box sx={{ p: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        {/* Search */}
        <TextField
          placeholder="Search by IP or Reason..."
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

        {/* Action Controls */}
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={handleExportCsv}
            sx={{
              borderColor: 'rgba(255, 255, 255, 0.08)',
              color: '#94a3b8',
              borderRadius: '12px',
              textTransform: 'none',
              fontWeight: 600,
              '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.02)', borderColor: 'rgba(255, 255, 255, 0.2)' },
            }}
          >
            Export CSV
          </Button>

          <Button
            component="label"
            variant="outlined"
            startIcon={<UploadIcon />}
            sx={{
              borderColor: 'rgba(255, 255, 255, 0.08)',
              color: '#94a3b8',
              borderRadius: '12px',
              textTransform: 'none',
              fontWeight: 600,
              '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.02)', borderColor: 'rgba(255, 255, 255, 0.2)' },
            }}
          >
            Import CSV
            <input type="file" accept=".csv" hidden onChange={handleImportCsv} />
          </Button>

          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenDialog}
            sx={{
              bgcolor: '#6366f1',
              borderRadius: '12px',
              textTransform: 'none',
              fontWeight: 700,
              boxShadow: '0 4px 14px rgba(99, 102, 241, 0.4)',
              '&:hover': { bgcolor: '#4f46e5' },
            }}
          >
            Block Custom IP
          </Button>
        </Box>
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
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>IP Address</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Reason</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Blocked At</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Status</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Expiry Time</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }} align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredList.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ color: '#64748b', py: 4, borderBottom: 'none' }}>
                      No blocked IP addresses found.
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredList.map((item) => (
                    <TableRow
                      key={item.id}
                      sx={{
                        borderBottom: '1px solid rgba(255, 255, 255, 0.04)',
                        '&:hover': { bgcolor: 'rgba(255, 255, 255, 0.01)' },
                      }}
                    >
                      <TableCell sx={{ color: '#fff', fontWeight: 600, borderBottom: 'none' }}>{item.ipAddress}</TableCell>
                      <TableCell sx={{ color: '#e2e8f0', borderBottom: 'none' }}>{item.reason}</TableCell>
                      <TableCell sx={{ color: '#94a3b8', borderBottom: 'none' }}>
                        {new Date(item.blockedAt).toLocaleString()}
                      </TableCell>
                      <TableCell sx={{ borderBottom: 'none' }}>
                        <span
                          style={{
                            padding: '4px 8px',
                            borderRadius: '6px',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            backgroundColor:
                              item.status === 'TEMPORARY'
                                ? 'rgba(251, 146, 60, 0.1)'
                                : 'rgba(244, 63, 94, 0.1)',
                            color: item.status === 'TEMPORARY' ? '#fb923c' : '#f43f5e',
                          }}
                        >
                          {item.status}
                        </span>
                      </TableCell>
                      <TableCell sx={{ color: '#94a3b8', borderBottom: 'none' }}>
                        {item.expiryTime ? new Date(item.expiryTime).toLocaleString() : 'Permanent'}
                      </TableCell>
                      <TableCell sx={{ borderBottom: 'none' }} align="right">
                        <IconButton
                          onClick={() => handleUnblockIp(item.ipAddress)}
                          sx={{
                            color: '#f43f5e',
                            '&:hover': { bgcolor: 'rgba(244, 63, 94, 0.1)' },
                          }}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Card>

      {/* Add Block Dialog */}
      <Dialog
        open={openAddDialog}
        onClose={handleCloseDialog}
        PaperProps={{
          sx: {
            bgcolor: '#0f172a',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            borderRadius: '20px',
            color: '#fff',
            p: 2,
            width: '100%',
            maxWidth: 460,
          },
        }}
      >
        <DialogTitle sx={{ fontWeight: 700, px: 3, pt: 2 }}>Block IP Address</DialogTitle>
        <form onSubmit={handleAddIp}>
          <DialogContent sx={{ px: 3, py: 1 }}>
            {formError && (
              <Alert severity="error" sx={{ mb: 2, borderRadius: '8px' }}>
                {formError}
              </Alert>
            )}
            {formSuccess && (
              <Alert severity="success" sx={{ mb: 2, borderRadius: '8px' }}>
                {formSuccess}
              </Alert>
            )}

            <TextField
              fullWidth
              label="IP Address"
              value={ipAddress}
              onChange={(e) => setIpAddress(e.target.value)}
              placeholder="e.g. 192.168.1.50"
              margin="dense"
              sx={{
                mb: 2,
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

            <TextField
              fullWidth
              label="Block Reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. Brute Force Login Attempts"
              margin="dense"
              sx={{
                mb: 2,
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

            <FormControl
              fullWidth
              sx={{
                mb: 2,
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
              <InputLabel>Block Policy</InputLabel>
              <Select value={status} label="Block Policy" onChange={(e) => setStatus(e.target.value)}>
                <MenuItem value="PERMANENT">Permanent Blacklist</MenuItem>
                <MenuItem value="TEMPORARY">Temporary Ban</MenuItem>
              </Select>
            </FormControl>

            {status === 'TEMPORARY' && (
              <TextField
                fullWidth
                label="Ban Duration (Hours)"
                type="number"
                value={durationHours}
                onChange={(e) => setDurationHours(e.target.value)}
                margin="dense"
                sx={{
                  mb: 1,
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
            )}
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={handleCloseDialog} sx={{ color: '#64748b', textTransform: 'none', fontWeight: 600 }}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="contained"
              sx={{
                bgcolor: '#6366f1',
                borderRadius: '10px',
                textTransform: 'none',
                fontWeight: 600,
                '&:hover': { bgcolor: '#4f46e5' },
              }}
            >
              Confirm Block
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default Blacklist;
