import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  Box,
  TextField,
  MenuItem,
  Grid,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Login as LoginIcon,
  Logout as LogoutIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  People as PeopleIcon,
  Notifications as NotificationIcon,
  Settings as SettingsIcon,
  Lock as LockIcon,
  History as HistoryIcon,
} from '@mui/icons-material';
import api from '../services/api';
import { format } from 'date-fns';

interface ActivityLogEntry {
  id: number;
  type: string;
  description: string;
  ipAddress: string;
  createdAt: string;
}

export default function ActivityLog() {
  const [activities, setActivities] = useState<ActivityLogEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [filterType, setFilterType] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const activityTypes = [
    'ALL',
    'LOGIN',
    'LOGOUT',
    'ASSET_CREATED',
    'ASSET_UPDATED',
    'ASSET_DELETED',
    'CONTACT_ADDED',
    'CONTACT_REMOVED',
    'CONTACT_VERIFIED',
    'INACTIVITY_CHECK',
    'NOTIFICATION_SENT',
    'VAULT_REVEALED',
    'SETTINGS_UPDATED',
  ];

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'LOGIN':
        return <LoginIcon fontSize="small" />;
      case 'LOGOUT':
        return <LogoutIcon fontSize="small" />;
      case 'ASSET_CREATED':
      case 'ASSET_UPDATED':
        return <EditIcon fontSize="small" />;
      case 'ASSET_DELETED':
        return <DeleteIcon fontSize="small" />;
      case 'CONTACT_ADDED':
      case 'CONTACT_REMOVED':
      case 'CONTACT_VERIFIED':
        return <PeopleIcon fontSize="small" />;
      case 'NOTIFICATION_SENT':
        return <NotificationIcon fontSize="small" />;
      case 'SETTINGS_UPDATED':
        return <SettingsIcon fontSize="small" />;
      case 'VAULT_REVEALED':
        return <LockIcon fontSize="small" />;
      default:
        return <HistoryIcon fontSize="small" />;
    }
  };

  const getActivityColor = (type: string): any => {
    if (type.includes('LOGIN') || type.includes('LOGOUT')) return 'info';
    if (type.includes('CREATED') || type.includes('ADDED')) return 'success';
    if (type.includes('DELETED') || type.includes('REMOVED')) return 'error';
    if (type.includes('NOTIFICATION') || type.includes('INACTIVITY')) return 'warning';
    if (type.includes('VERIFIED') || type.includes('REVEALED')) return 'secondary';
    return 'default';
  };

  const fetchActivities = async () => {
    setLoading(true);
    try {
      const response = await api.get('/users/activity-logs');
      setActivities(response.data);
    } catch (error) {
      console.error('Failed to fetch activity logs:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchActivities();
  }, []);

  const filteredActivities = activities.filter((activity) => {
    const matchesType = filterType === 'ALL' || activity.type === filterType;
    const matchesSearch =
      searchTerm === '' ||
      activity.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      activity.type.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesType && matchesSearch;
  });

  const handleChangePage = (_event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const isDemoActivity = (description: string) => {
    return description.toLowerCase().includes('demo:');
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4">
          Activity Log
        </Typography>
        <Tooltip title="Refresh">
          <IconButton onClick={fetchActivities} color="primary">
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              label="Search"
              variant="outlined"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search activities..."
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              select
              label="Activity Type"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              {activityTypes.map((type) => (
                <MenuItem key={type} value={type}>
                  {type.replace(/_/g, ' ')}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Time</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>IP Address</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredActivities
              .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
              .map((activity) => (
                <TableRow 
                  key={activity.id}
                  sx={{ 
                    backgroundColor: isDemoActivity(activity.description) ? 'rgba(255, 193, 7, 0.08)' : 'transparent'
                  }}
                >
                  <TableCell>
                    {format(new Date(activity.createdAt), 'MMM dd, yyyy HH:mm:ss')}
                  </TableCell>
                  <TableCell>
                    <Chip
                      icon={getActivityIcon(activity.type)}
                      label={activity.type.replace(/_/g, ' ')}
                      color={getActivityColor(activity.type)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {isDemoActivity(activity.description) && (
                      <Chip label="DEMO" size="small" color="warning" sx={{ mr: 1 }} />
                    )}
                    {activity.description}
                  </TableCell>
                  <TableCell>{activity.ipAddress || 'N/A'}</TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
        {filteredActivities.length === 0 && !loading && (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <Typography color="text.secondary">
              No activities found
            </Typography>
          </Box>
        )}
      </TableContainer>

      <TablePagination
        rowsPerPageOptions={[10, 25, 50, 100]}
        component="div"
        count={filteredActivities.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Container>
  );
}