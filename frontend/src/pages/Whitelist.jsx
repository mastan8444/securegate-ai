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
  CircularProgress,
  Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import DownloadIcon from '@mui/icons-material/Download';
import UploadIcon from '@mui/icons-material/Upload';
import SearchIcon from '@mui/icons-material/Search';

const Whitelist = () => {
  const [whitelist, setWhitelist] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [openAddDialog, setOpenAddDialog] = useState(false);

  // Form fields
  const [ipAddress, setIpAddress] = useState('');
  const [owner, setOwner] = useState('');
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');

  const fetchWhitelist = async () => {
    try {
      const response = await API.get('/whitelist');
      setWhitelist(response.data);
    } catch (error) {
      console.error('Error fetching whitelist:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWhitelist();
  }, []);

  const handleOpenDialog = () => {
    setIpAddress('');
    setOwner('');
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
      await API.post('/whitelist', { ipAddress, owner });
      setFormSuccess('IP whitelisted successfully!');
      fetchWhitelist();
      setTimeout(handleCloseDialog, 1000);
    } catch (error) {
      setFormError(error.response?.data?.message || 'Failed to whitelist IP');
    }
  };

  const handleRemoveIp = async (ip) => {
    if (window.confirm(`Are you sure you want to remove IP ${ip} from the whitelist?`)) {
      try {
        await API.delete(`/whitelist/${ip}`);
        fetchWhitelist();
      } catch (error) {
        console.error('Failed to remove from whitelist:', error);
      }
    }
  };

  const handleExportCsv = () => {
    API.get('/export/whitelist', { responseType: 'blob' })
      .then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'securegate_whitelist.csv');
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
      await API.post('/import/whitelist', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      alert('CSV whitelist imported successfully!');
      fetchWhitelist();
    } catch (error) {
      alert('Failed to import CSV: ' + (error.response?.data || error.message));
    } finally {
      setLoading(false);
    }
  };

  const filteredList = whitelist.filter(
    (item) =>
      item.ipAddress.includes(searchQuery) ||
      item.owner.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <Box sx={{ p: 4 }}>
      <Box sx={{ display: 'flex', justifySelf: 'stretch', justifyContent: 'space-between', alignItems: 'center', mb: 4, width: '100%' }}>
        {/* Search */}
        <TextField
          placeholder="Search by IP or Owner..."
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
            Whitelist Trusted IP
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
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Owner / Location</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }}>Added At</TableCell>
                  <TableCell sx={{ color: '#64748b', fontWeight: 600, borderBottom: 'none' }} align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredList.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center" sx={{ color: '#64748b', py: 4, borderBottom: 'none' }}>
                      No whitelisted IP addresses found.
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
                      <TableCell sx={{ color: '#e2e8f0', borderBottom: 'none' }}>{item.owner}</TableCell>
                      <TableCell sx={{ color: '#94a3b8', borderBottom: 'none' }}>
                        {new Date(item.addedAt).toLocaleString()}
                      </TableCell>
                      <TableCell sx={{ borderBottom: 'none' }} align="right">
                        <IconButton
                          onClick={() => handleRemoveIp(item.ipAddress)}
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

      {/* Add Whitelist Dialog */}
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
        <DialogTitle sx={{ fontWeight: 700, px: 3, pt: 2 }}>Whitelist Trusted IP</DialogTitle>
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
              placeholder="e.g. 192.168.1.10"
              margin="dense"
              sx={{
                mb: 2.5,
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
              label="Owner / Description"
              value={owner}
              onChange={(e) => setOwner(e.target.value)}
              placeholder="e.g. CEO Home Office / Server Gateway"
              margin="dense"
              sx={{
                mb: 1.5,
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
              Confirm Trust
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  );
};

export default Whitelist;
