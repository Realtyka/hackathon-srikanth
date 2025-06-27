import React from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Button,
  Avatar,
  useTheme,
  useMediaQuery,
  Divider,
  Container,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  AccountBalance as AssetsIcon,
  People as ContactsIcon,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  Shield as ShieldIcon,
  Close as CloseIcon,
  PlayCircleOutline as DemoIcon,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const drawerWidth = 280;

const baseMenuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { text: 'Assets', icon: <AssetsIcon />, path: '/assets' },
  { text: 'Trusted Contacts', icon: <ContactsIcon />, path: '/contacts' },
  { text: 'Settings', icon: <SettingsIcon />, path: '/settings' },
];

// Add demo menu item if in demo mode (check for localhost or specific env var)
const isDemoMode = window.location.hostname === 'localhost' || process.env.REACT_APP_DEMO_MODE === 'true';
const menuItems = isDemoMode 
  ? [...baseMenuItems, { text: 'Demo', icon: <DemoIcon />, path: '/demo' }]
  : baseMenuItems;

const Layout: React.FC = () => {
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const { user, logout } = useAuth();
  const location = useLocation();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const navigate = useNavigate();

  const handleNavigation = (path: string) => {
    navigate(path);
    if (isMobile) {
      setMobileOpen(false);
    }
  };

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Toolbar
        sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #1565c0 100%)',
          color: 'white',
          minHeight: { xs: 56, sm: 64 },
        }}
      >
        <ShieldIcon sx={{ mr: 2 }} />
        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1, fontWeight: 600 }}>
          Life Vault
        </Typography>
        {isMobile && (
          <IconButton
            color="inherit"
            edge="end"
            onClick={handleDrawerToggle}
            sx={{ ml: 'auto' }}
          >
            <CloseIcon />
          </IconButton>
        )}
      </Toolbar>
      <Box sx={{ p: 2 }}>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            p: 2,
            backgroundColor: theme.palette.grey[50],
            borderRadius: 2,
            mb: 2,
          }}
        >
          <Avatar
            sx={{
              width: 48,
              height: 48,
              bgcolor: theme.palette.primary.main,
              mr: 2,
            }}
          >
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </Avatar>
          <Box>
            <Typography variant="subtitle2" fontWeight={600}>
              {user?.firstName} {user?.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {user?.email}
            </Typography>
          </Box>
        </Box>
      </Box>
      <Divider />
      <List sx={{ px: 2, py: 1 }}>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
            <ListItemButton
              onClick={() => handleNavigation(item.path)}
              selected={location.pathname === item.path}
              sx={{
                borderRadius: 2,
                '&.Mui-selected': {
                  backgroundColor: theme.palette.primary.main,
                  color: 'white',
                  '& .MuiListItemIcon-root': {
                    color: 'white',
                  },
                  '&:hover': {
                    backgroundColor: theme.palette.primary.dark,
                  },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText 
                primary={item.text} 
                primaryTypographyProps={{ 
                  fontSize: '0.95rem',
                  fontWeight: location.pathname === item.path ? 600 : 400,
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Box sx={{ mt: 'auto', p: 2 }}>
        <Button
          fullWidth
          variant="outlined"
          color="error"
          startIcon={<LogoutIcon />}
          onClick={logout}
          sx={{ borderRadius: 2 }}
        >
          Logout
        </Button>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
          backgroundColor: 'white',
          color: theme.palette.text.primary,
          boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
        }}
      >
        <Toolbar sx={{ minHeight: { xs: 56, sm: 64 } }}>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
            {isMobile && (
              <>
                <ShieldIcon sx={{ mr: 1, color: theme.palette.primary.main }} />
                <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 600 }}>
                  Life Vault
                </Typography>
              </>
            )}
            {!isMobile && (
              <Typography variant="h6" sx={{ flexGrow: 1, color: theme.palette.text.secondary }}>
                Secure your legacy
              </Typography>
            )}
          </Box>
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              borderRadius: 0,
              borderRight: 'none',
            },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
              borderRight: `1px solid ${theme.palette.divider}`,
              backgroundColor: theme.palette.background.paper,
            },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          backgroundColor: theme.palette.background.default,
          minHeight: '100vh',
        }}
      >
        <Toolbar />
        <Container
          maxWidth="xl"
          sx={{
            py: { xs: 2, sm: 3 },
            px: { xs: 2, sm: 3 },
          }}
        >
          <Outlet />
        </Container>
      </Box>
    </Box>
  );
};

export default Layout;