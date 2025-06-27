import React from 'react';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  useTheme,
  LinearProgress,
  Chip,
  Button,
} from '@mui/material';
import {
  AccountBalance,
  People,
  Timer,
  CheckCircle,
  TrendingUp,
  Security,
  ArrowForward,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const Dashboard: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const { data: stats, isLoading } = useQuery('dashboardStats', async () => {
    const [assets, contacts, profile] = await Promise.all([
      api.get('/assets'),
      api.get('/contacts'),
      api.get('/users/profile'),
    ]);
    return {
      assetsCount: assets.data.length,
      contactsCount: contacts.data.length,
      inactivityPeriodDays: profile.data.inactivityPeriodDays || 180,
    };
  });
  
  const formatInactivityPeriod = (days: number) => {
    if (days === 90) return '3 months';
    if (days === 180) return '6 months';
    if (days === 365) return '1 year';
    if (days === 730) return '2 years';
    return `${days} days`;
  };

  const statCards = [
    {
      title: 'Total Assets',
      value: stats?.assetsCount || 0,
      icon: <AccountBalance sx={{ fontSize: 28 }} />,
      color: theme.palette.primary.main,
      bgColor: theme.palette.primary.light + '20',
      action: () => navigate('/assets'),
      actionText: 'Manage Assets',
    },
    {
      title: 'Trusted Contacts',
      value: stats?.contactsCount || 0,
      icon: <People sx={{ fontSize: 28 }} />,
      color: theme.palette.secondary.main,
      bgColor: theme.palette.secondary.light + '20',
      action: () => navigate('/contacts'),
      actionText: 'Add Contacts',
    },
    {
      title: 'Inactivity Period',
      value: formatInactivityPeriod(stats?.inactivityPeriodDays || 180),
      icon: <Timer sx={{ fontSize: 28 }} />,
      color: theme.palette.warning.main,
      bgColor: theme.palette.warning.light + '20',
      action: () => navigate('/settings'),
      actionText: 'Update Settings',
    },
  ];

  if (isLoading) {
    return <LinearProgress />;
  }

  return (
    <Box>
      {/* Welcome Section */}
      <Box mb={4}>
        <Typography variant="h4" fontWeight={700} gutterBottom>
          Welcome back, {user?.firstName}!
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Your digital vault is secure and protected
        </Typography>
      </Box>
      
      {/* Stats Grid */}
      <Grid container spacing={3} mb={4}>
        {statCards.map((stat, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <Card
              sx={{
                height: '100%',
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: theme.shadows[8],
                },
              }}
              onClick={stat.action}
            >
              <CardContent sx={{ p: 3 }}>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={3}>
                  <Box
                    sx={{
                      backgroundColor: stat.bgColor,
                      color: stat.color,
                      p: 1.5,
                      borderRadius: 2,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {stat.icon}
                  </Box>
                  <Chip
                    icon={<TrendingUp fontSize="small" />}
                    label="Active"
                    size="small"
                    color="success"
                    sx={{ ml: 1 }}
                  />
                </Box>
                <Typography color="text.secondary" variant="body2" gutterBottom>
                  {stat.title}
                </Typography>
                <Typography variant="h3" fontWeight={600} sx={{ mb: 2 }}>
                  {stat.value}
                </Typography>
                <Button
                  size="small"
                  endIcon={<ArrowForward />}
                  sx={{ textTransform: 'none' }}
                >
                  {stat.actionText}
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
        
      </Grid>

      {/* How it Works Section */}
      <Grid container spacing={3}>
        <Grid item xs={12} lg={8}>
          <Paper sx={{ p: { xs: 3, sm: 4 }, height: '100%' }}>
            <Box display="flex" alignItems="center" mb={3}>
              <Security sx={{ mr: 2, color: theme.palette.primary.main }} />
              <Typography variant="h5" fontWeight={600}>
                How Life Vault Protects Your Legacy
              </Typography>
            </Box>
            
            <Grid container spacing={3}>
              {[
                {
                  step: '1',
                  title: 'Secure Your Assets',
                  desc: 'Add descriptions of your important assets. No sensitive account numbers needed.',
                  icon: <AccountBalance />,
                },
                {
                  step: '2',
                  title: 'Designate Contacts',
                  desc: 'Choose trusted people who should access your information if needed.',
                  icon: <People />,
                },
                {
                  step: '3',
                  title: 'Stay Active',
                  desc: 'We check in periodically. Simply log in or respond to confirm you\'re okay.',
                  icon: <CheckCircle />,
                },
                {
                  step: '4',
                  title: 'Automatic Protection',
                  desc: 'If inactive for your set period, trusted contacts are notified and given access.',
                  icon: <Timer />,
                },
              ].map((item, index) => (
                <Grid item xs={12} sm={6} key={index}>
                  <Box display="flex" alignItems="flex-start">
                    <Box
                      sx={{
                        backgroundColor: theme.palette.primary.main,
                        color: 'white',
                        borderRadius: '50%',
                        width: 40,
                        height: 40,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mr: 2,
                        flexShrink: 0,
                      }}
                    >
                      <Typography variant="h6" fontWeight={700}>
                        {item.step}
                      </Typography>
                    </Box>
                    <Box>
                      <Typography variant="h6" gutterBottom fontWeight={600}>
                        {item.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {item.desc}
                      </Typography>
                    </Box>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Paper>
        </Grid>
        
        {/* Quick Actions */}
        <Grid item xs={12} lg={4}>
          <Paper sx={{ p: { xs: 3, sm: 4 }, height: '100%' }}>
            <Typography variant="h6" fontWeight={600} gutterBottom>
              Quick Actions
            </Typography>
            <Box display="flex" flexDirection="column" gap={2} mt={3}>
              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={() => navigate('/assets')}
                startIcon={<AccountBalance />}
                sx={{ justifyContent: 'flex-start' }}
              >
                Add New Asset
              </Button>
              <Button
                variant="outlined"
                fullWidth
                size="large"
                onClick={() => navigate('/contacts')}
                startIcon={<People />}
                sx={{ justifyContent: 'flex-start' }}
              >
                Add Trusted Contact
              </Button>
              <Button
                variant="outlined"
                fullWidth
                size="large"
                onClick={() => navigate('/settings')}
                startIcon={<Timer />}
                sx={{ justifyContent: 'flex-start' }}
              >
                Update Inactivity Period
              </Button>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;