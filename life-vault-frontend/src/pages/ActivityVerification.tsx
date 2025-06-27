import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  CircularProgress,
  Button,
  Alert,
} from '@mui/material';
import {
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import axios from 'axios';

const ActivityVerification: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [loading, setLoading] = useState(true);
  const [verified, setVerified] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const verifyActivity = async () => {
      try {
        const response = await axios.get(`/api/activity/verify/${token}`);
        if (response.data.status === 'success') {
          setVerified(true);
        } else {
          setError(response.data.message || 'Verification failed');
        }
      } catch (err: any) {
        setError(err.response?.data?.message || 'Invalid or expired verification link');
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      verifyActivity();
    }
  }, [token]);

  if (loading) {
    return (
      <Container component="main" maxWidth="sm">
        <Box
          sx={{
            marginTop: 8,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <CircularProgress />
          <Typography sx={{ mt: 2 }}>Verifying your activity...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ padding: 4, width: '100%', textAlign: 'center' }}>
          {verified ? (
            <>
              <SuccessIcon sx={{ fontSize: 80, color: 'success.main', mb: 2 }} />
              <Typography component="h1" variant="h4" gutterBottom>
                Activity Confirmed!
              </Typography>
              <Typography variant="body1" color="text.secondary" paragraph>
                Thank you for confirming you're active. Your inactivity timer has been reset.
              </Typography>
              <Alert severity="success" sx={{ mt: 2, mb: 3 }}>
                Your next check-in will be based on your inactivity period settings.
              </Alert>
              <Button
                component={Link}
                to="/login"
                variant="contained"
                fullWidth
              >
                Go to Login
              </Button>
            </>
          ) : (
            <>
              <ErrorIcon sx={{ fontSize: 80, color: 'error.main', mb: 2 }} />
              <Typography component="h1" variant="h4" gutterBottom>
                Verification Failed
              </Typography>
              <Typography variant="body1" color="text.secondary" paragraph>
                {error}
              </Typography>
              <Alert severity="error" sx={{ mt: 2, mb: 3 }}>
                This link may have expired or already been used. Please check your latest email
                or log in to your account.
              </Alert>
              <Button
                component={Link}
                to="/login"
                variant="contained"
                fullWidth
              >
                Go to Login
              </Button>
            </>
          )}
        </Paper>
      </Box>
    </Container>
  );
};

export default ActivityVerification;