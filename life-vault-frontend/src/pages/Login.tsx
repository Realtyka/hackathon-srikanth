import React from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  useTheme,
  Divider,
  IconButton,
  InputAdornment,
} from '@mui/material';
import {
  Shield as ShieldIcon,
  Visibility,
  VisibilityOff,
  Security as SecurityIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { LoginCredentials } from '../types';

const Login: React.FC = () => {
  const { login } = useAuth();
  const [error, setError] = React.useState('');
  const [showPassword, setShowPassword] = React.useState(false);
  const theme = useTheme();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginCredentials>();

  const onSubmit = async (data: LoginCredentials) => {
    try {
      setError('');
      await login(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: theme.palette.background.default,
        py: { xs: 2, sm: 0 },
      }}
    >
      <Container component="main" maxWidth="sm">
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          {/* Logo and Title */}
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              mb: 4,
            }}
          >
            <ShieldIcon
              sx={{
                fontSize: 48,
                color: theme.palette.primary.main,
                mr: 2,
              }}
            />
            <Box>
              <Typography variant="h4" component="h1" fontWeight={700}>
                Life Vault
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Secure your digital legacy
              </Typography>
            </Box>
          </Box>

          <Paper
            elevation={0}
            sx={{
              p: { xs: 3, sm: 5 },
              width: '100%',
              maxWidth: 450,
              borderRadius: 3,
              border: `1px solid ${theme.palette.divider}`,
            }}
          >
            <Typography component="h2" variant="h5" align="center" gutterBottom fontWeight={600}>
              Welcome Back
            </Typography>
            <Typography variant="body2" color="text.secondary" align="center" sx={{ mb: 3 }}>
              Sign in to access your vault
            </Typography>

            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit(onSubmit)}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email Address"
                autoComplete="email"
                autoFocus
                {...register('email', {
                  required: 'Email is required',
                  pattern: {
                    value: /^\S+@\S+$/i,
                    message: 'Invalid email address',
                  },
                })}
                error={!!errors.email}
                helperText={errors.email?.message}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                label="Password"
                type={showPassword ? 'text' : 'password'}
                id="password"
                autoComplete="current-password"
                {...register('password', {
                  required: 'Password is required',
                  minLength: {
                    value: 8,
                    message: 'Password must be at least 8 characters',
                  },
                })}
                error={!!errors.password}
                helperText={errors.password?.message}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle password visibility"
                        onClick={() => setShowPassword(!showPassword)}
                        edge="end"
                      >
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                sx={{
                  mt: 3,
                  mb: 2,
                  py: 1.5,
                  textTransform: 'none',
                  fontSize: '1rem',
                  fontWeight: 600,
                }}
                disabled={isSubmitting}
              >
                {isSubmitting ? 'Signing In...' : 'Sign In'}
              </Button>
              
              <Divider sx={{ my: 3 }}>
                <Typography variant="body2" color="text.secondary">
                  OR
                </Typography>
              </Divider>
              
              <Box textAlign="center">
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Don't have an account?
                </Typography>
                <Link to="/signup" style={{ textDecoration: 'none' }}>
                  <Button variant="text" size="large" sx={{ textTransform: 'none' }}>
                    Create Account
                  </Button>
                </Link>
              </Box>
              
              {/* Security Notice */}
              <Box
                sx={{
                  mt: 4,
                  p: 2,
                  backgroundColor: theme.palette.grey[50],
                  borderRadius: 2,
                  display: 'flex',
                  alignItems: 'center',
                }}
              >
                <SecurityIcon
                  sx={{
                    color: theme.palette.primary.main,
                    mr: 1,
                    fontSize: 20,
                  }}
                />
                <Typography variant="caption" color="text.secondary">
                  Your data is encrypted and secured with bank-level security
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Box>
      </Container>
    </Box>
  );
};

export default Login;