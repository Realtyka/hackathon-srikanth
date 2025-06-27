import React from 'react';
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
} from '@mui/material';
import {
  AccountBalance,
  People,
  Timer,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import api from '../services/api';

const Dashboard: React.FC = () => {
  const { data: stats } = useQuery('dashboardStats', async () => {
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
      icon: <AccountBalance />,
      color: '#1976d2',
    },
    {
      title: 'Trusted Contacts',
      value: stats?.contactsCount || 0,
      icon: <People />,
      color: '#388e3c',
    },
    {
      title: 'Inactivity Period',
      value: formatInactivityPeriod(stats?.inactivityPeriodDays || 180),
      icon: <Timer />,
      color: '#7b1fa2',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>
      
      <Grid container spacing={3}>
        {statCards.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  <Box
                    sx={{
                      backgroundColor: stat.color,
                      color: 'white',
                      p: 1,
                      borderRadius: 1,
                      mr: 2,
                    }}
                  >
                    {stat.icon}
                  </Box>
                  <Typography color="textSecondary" gutterBottom>
                    {stat.title}
                  </Typography>
                </Box>
                <Typography variant="h4">{stat.value}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
        
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              How Life Vault Works
            </Typography>
            <Box sx={{ mt: 2 }}>
              <Typography variant="body1" paragraph>
                1. <strong>Add your assets</strong> - Store descriptions of your important assets
                (no sensitive account numbers needed).
              </Typography>
              <Typography variant="body1" paragraph>
                2. <strong>Add trusted contacts</strong> - Choose people who should have access
                to your information if you become inactive.
              </Typography>
              <Typography variant="body1" paragraph>
                3. <strong>Stay active</strong> - We'll check in with you periodically. Just
                log in or respond to our emails to confirm you're okay.
              </Typography>
              <Typography variant="body1" paragraph>
                4. <strong>Automatic reveal</strong> - If you don't respond for 6+ months,
                your trusted contacts will be notified and given access to your asset information.
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;