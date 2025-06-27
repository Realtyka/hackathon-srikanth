import React, { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Typography,
  Button,
  Box,
  Alert,
  Grid,
  Card,
  CardContent,
  CardActions,
  Chip,
  CircularProgress,
  Slider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  PlayArrow,
  Timer,
  Email,
  Info,
  CheckCircle,
  Warning,
} from '@mui/icons-material';
import { api } from '../services/api';

interface DemoStatus {
  demoMode: boolean;
  schedulerInterval: string;
  userEmail: string;
  lastActivityDate: string;
  inactivityPeriodDays: number;
  currentDaysInactive: number;
  has50PercentWarning: boolean;
  has75PercentWarning: boolean;
}

const Demo: React.FC = () => {
  const [status, setStatus] = useState<DemoStatus | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error' | 'info'; text: string } | null>(null);
  const [daysInactive, setDaysInactive] = useState(135);
  const [scenario, setScenario] = useState('50percent');

  const scenarios = [
    { value: '50percent', label: '50% Warning', days: 135, description: '45 days before deadline' },
    { value: '75percent', label: '75% Warning', days: 157, description: '22.5 days before deadline' },
    { value: 'finalweek', label: 'Final Week', days: 173, description: 'Daily warnings' },
    { value: 'graceperiod', label: 'Grace Period', days: 181, description: 'Every 2 days warnings' },
    { value: 'expired', label: 'Expired', days: 195, description: 'Contacts notified' },
  ];

  useEffect(() => {
    fetchStatus();
  }, []);

  const fetchStatus = async () => {
    try {
      const response = await api.get('/demo/status');
      setStatus(response.data);
    } catch (error) {
      console.error('Failed to fetch demo status:', error);
    }
  };

  const handleScenarioChange = (value: string) => {
    setScenario(value);
    const selectedScenario = scenarios.find(s => s.value === value);
    if (selectedScenario) {
      setDaysInactive(selectedScenario.days);
    }
  };

  const simulateInactivity = async () => {
    setLoading(true);
    try {
      const response = await api.post(`/demo/simulate-inactivity?daysInactive=${daysInactive}`);
      setMessage({ type: 'success', text: `Simulated ${daysInactive} days of inactivity` });
      await fetchStatus();
    } catch (error) {
      setMessage({ type: 'error', text: 'Failed to simulate inactivity' });
    }
    setLoading(false);
  };

  const triggerCheck = async () => {
    setLoading(true);
    try {
      await api.post('/demo/trigger-check');
      setMessage({ type: 'info', text: 'Inactivity check triggered! Check your email.' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Failed to trigger check' });
    }
    setLoading(false);
  };

  const getWarningStatus = () => {
    if (!status || !status.currentDaysInactive) return null;
    
    const days = status.currentDaysInactive;
    const period = status.inactivityPeriodDays;
    
    if (days > period + 14) return { label: 'Expired', color: 'error' };
    if (days > period) return { label: 'Grace Period', color: 'warning' };
    if (days > period - 7) return { label: 'Final Week', color: 'warning' };
    if (days > period * 0.75) return { label: '75% Warning', color: 'info' };
    if (days > period * 0.5) return { label: '50% Warning', color: 'info' };
    return { label: 'Active', color: 'success' };
  };

  const warningStatus = getWarningStatus();

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
          <PlayArrow sx={{ mr: 1 }} />
          Demo Mode Dashboard
        </Typography>
        <Alert severity="info" sx={{ mb: 2 }}>
          Demo mode is active. The scheduler runs every minute instead of daily.
        </Alert>
      </Box>

      {message && (
        <Alert severity={message.type} onClose={() => setMessage(null)} sx={{ mb: 3 }}>
          {message.text}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Current Status */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                <Info sx={{ mr: 1 }} />
                Current Status
              </Typography>
              {status && (
                <Box sx={{ mt: 2 }}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Email: {status.userEmail}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Last Activity: {new Date(status.lastActivityDate).toLocaleDateString()}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Days Inactive: {status.currentDaysInactive || 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Inactivity Period: {status.inactivityPeriodDays} days
                  </Typography>
                  <Box sx={{ mt: 2 }}>
                    {warningStatus && (
                      <Chip 
                        label={warningStatus.label} 
                        color={warningStatus.color as any}
                        icon={warningStatus.color === 'success' ? <CheckCircle /> : <Warning />}
                      />
                    )}
                  </Box>
                  <Box sx={{ mt: 2 }}>
                    {status.has50PercentWarning && (
                      <Chip label="50% Warning Sent" size="small" sx={{ mr: 1 }} />
                    )}
                    {status.has75PercentWarning && (
                      <Chip label="75% Warning Sent" size="small" />
                    )}
                  </Box>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Simulate Inactivity */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                <Timer sx={{ mr: 1 }} />
                Simulate Inactivity
              </Typography>
              
              <FormControl fullWidth sx={{ mt: 2, mb: 2 }}>
                <InputLabel>Scenario</InputLabel>
                <Select
                  value={scenario}
                  onChange={(e) => handleScenarioChange(e.target.value)}
                  label="Scenario"
                >
                  {scenarios.map((s) => (
                    <MenuItem key={s.value} value={s.value}>
                      <Box>
                        <Typography>{s.label}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {s.description}
                        </Typography>
                      </Box>
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <Typography gutterBottom>Days Inactive: {daysInactive}</Typography>
              <Slider
                value={daysInactive}
                onChange={(_, value) => setDaysInactive(value as number)}
                min={0}
                max={250}
                marks={[
                  { value: 0, label: '0' },
                  { value: 90, label: '90' },
                  { value: 135, label: '135' },
                  { value: 180, label: '180' },
                  { value: 250, label: '250' },
                ]}
                valueLabelDisplay="auto"
              />
            </CardContent>
            <CardActions>
              <Button
                variant="contained"
                onClick={simulateInactivity}
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <PlayArrow />}
              >
                Simulate
              </Button>
            </CardActions>
          </Card>
        </Grid>

        {/* Trigger Actions */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                <Email sx={{ mr: 1 }} />
                Trigger Notifications
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                Manually trigger the inactivity check to send notifications based on current status.
              </Typography>
            </CardContent>
            <CardActions>
              <Button
                variant="contained"
                color="secondary"
                onClick={triggerCheck}
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <Email />}
              >
                Trigger Check Now
              </Button>
            </CardActions>
          </Card>
        </Grid>

        {/* Instructions */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Demo Instructions
            </Typography>
            <Box component="ol" sx={{ pl: 2 }}>
              <Typography component="li" paragraph>
                Select a scenario or set custom days of inactivity
              </Typography>
              <Typography component="li" paragraph>
                Click "Simulate" to backdate your last activity
              </Typography>
              <Typography component="li" paragraph>
                Click "Trigger Check Now" to run the notification scheduler
              </Typography>
              <Typography component="li" paragraph>
                Check your email for the appropriate warning message
              </Typography>
              <Typography component="li" paragraph>
                The scheduler also runs automatically every minute in demo mode
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Demo;