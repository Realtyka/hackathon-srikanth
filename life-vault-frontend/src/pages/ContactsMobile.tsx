import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  IconButton,
  Chip,
  Grid,
  useTheme,
  Avatar,
  Divider,
} from '@mui/material';
import {
  Delete as DeleteIcon,
  CheckCircle as VerifiedIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { TrustedContact } from '../types';

interface ContactCardProps {
  contact: TrustedContact;
  onDelete: (id: number) => void;
}

const ContactCard: React.FC<ContactCardProps> = ({ contact, onDelete }) => {
  const theme = useTheme();

  return (
    <Card
      sx={{
        height: '100%',
        transition: 'all 0.3s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: theme.shadows[4],
        },
      }}
    >
      <CardContent sx={{ p: 3 }}>
        {/* Header */}
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Box display="flex" alignItems="center">
            <Avatar
              sx={{
                bgcolor: theme.palette.primary.light,
                color: theme.palette.primary.main,
                mr: 2,
                width: 48,
                height: 48,
              }}
            >
              {contact.name.charAt(0).toUpperCase()}
            </Avatar>
            <Box>
              <Typography variant="h6" fontWeight={600}>
                {contact.name}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {contact.relationship}
              </Typography>
            </Box>
          </Box>
          <IconButton
            size="small"
            onClick={() => onDelete(contact.id!)}
            sx={{
              color: theme.palette.error.main,
              '&:hover': {
                backgroundColor: theme.palette.error.light + '20',
              },
            }}
          >
            <DeleteIcon />
          </IconButton>
        </Box>

        <Divider sx={{ my: 2 }} />

        {/* Contact Info */}
        <Box display="flex" flexDirection="column" gap={1.5}>
          <Box display="flex" alignItems="center" gap={1}>
            <EmailIcon fontSize="small" color="action" />
            <Typography variant="body2" noWrap sx={{ flex: 1 }}>
              {contact.email}
            </Typography>
          </Box>

          {contact.phoneNumber && (
            <Box display="flex" alignItems="center" gap={1}>
              <PhoneIcon fontSize="small" color="action" />
              <Typography variant="body2">
                {contact.phoneNumber}
              </Typography>
            </Box>
          )}

          {contact.address && (
            <Box display="flex" alignItems="flex-start" gap={1}>
              <LocationIcon fontSize="small" color="action" sx={{ mt: 0.5 }} />
              <Typography variant="body2" sx={{ flex: 1 }}>
                {contact.address}
              </Typography>
            </Box>
          )}
        </Box>

        {/* Status */}
        <Box display="flex" justifyContent="flex-end" mt={2}>
          <Chip
            icon={<VerifiedIcon />}
            label="Active"
            color="success"
            size="small"
          />
        </Box>
      </CardContent>
    </Card>
  );
};

interface ContactsMobileProps {
  contacts: TrustedContact[];
  onDelete: (id: number) => void;
  isLoading: boolean;
}

export const ContactsMobile: React.FC<ContactsMobileProps> = ({
  contacts,
  onDelete,
  isLoading,
}) => {
  const theme = useTheme();

  if (isLoading) {
    return (
      <Box textAlign="center" py={4}>
        <Typography color="text.secondary">Loading...</Typography>
      </Box>
    );
  }

  if (contacts.length === 0) {
    return (
      <Box
        sx={{
          textAlign: 'center',
          py: 6,
          px: 2,
          backgroundColor: theme.palette.grey[50],
          borderRadius: 2,
        }}
      >
        <PersonIcon sx={{ fontSize: 64, color: theme.palette.grey[400], mb: 2 }} />
        <Typography variant="h6" gutterBottom>
          No trusted contacts yet
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Add someone you trust to access your information when needed
        </Typography>
      </Box>
    );
  }

  return (
    <Grid container spacing={2}>
      {contacts.map((contact) => (
        <Grid item xs={12} sm={6} md={4} key={contact.id}>
          <ContactCard contact={contact} onDelete={onDelete} />
        </Grid>
      ))}
    </Grid>
  );
};