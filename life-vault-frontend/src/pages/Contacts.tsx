import React, { useState } from 'react';
import {
  Box,
  Button,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid,
  Alert,
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  CheckCircle as VerifiedIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../services/api';
import { TrustedContact } from '../types';

const Contacts: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const queryClient = useQueryClient();

  const { data: contacts = [], isLoading } = useQuery<TrustedContact[]>(
    'contacts',
    async () => {
      const response = await api.get('/contacts');
      return response.data;
    }
  );

  const addContactMutation = useMutation(
    (contact: Omit<TrustedContact, 'id'>) => api.post('/contacts', contact),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('contacts');
        toast.success('Contact added successfully');
        handleCloseDialog();
      },
    }
  );

  const deleteContactMutation = useMutation(
    (id: number) => api.delete(`/contacts/${id}`),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('contacts');
        toast.success('Contact removed successfully');
      },
    }
  );

  const handleOpenDialog = () => {
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to remove this contact?')) {
      deleteContactMutation.mutate(id);
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Trusted Contacts</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenDialog}
        >
          Add Contact
        </Button>
      </Box>

      <Alert severity="info" sx={{ mb: 3 }}>
        Trusted contacts will receive access to your asset information if you become 
        inactive for your specified period (default: 6 months). They will NOT be 
        notified when you add them - they'll only be contacted if needed.
      </Alert>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Address</TableCell>
              <TableCell>Relationship</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  Loading...
                </TableCell>
              </TableRow>
            ) : contacts.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  No trusted contacts added yet. Add someone you trust to access your information.
                </TableCell>
              </TableRow>
            ) : (
              contacts.map((contact) => (
                <TableRow key={contact.id}>
                  <TableCell>{contact.name}</TableCell>
                  <TableCell>
                    <Box display="flex" alignItems="center" gap={1}>
                      <EmailIcon fontSize="small" color="action" />
                      {contact.email}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {contact.phoneNumber ? (
                      <Box display="flex" alignItems="center" gap={1}>
                        <PhoneIcon fontSize="small" color="action" />
                        {contact.phoneNumber}
                      </Box>
                    ) : (
                      '-'
                    )}
                  </TableCell>
                  <TableCell>
                    {contact.address ? (
                      <Box display="flex" alignItems="center" gap={1}>
                        <LocationIcon fontSize="small" color="action" />
                        {contact.address}
                      </Box>
                    ) : (
                      '-'
                    )}
                  </TableCell>
                  <TableCell>{contact.relationship}</TableCell>
                  <TableCell>
                    <Chip
                      icon={<VerifiedIcon />}
                      label="Active"
                      color="success"
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleDelete(contact.id!)}
                      color="error"
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

      <AddContactDialog
        open={openDialog}
        onClose={handleCloseDialog}
        onSave={(contact) => addContactMutation.mutate(contact)}
      />
    </Box>
  );
};

interface AddContactDialogProps {
  open: boolean;
  onClose: () => void;
  onSave: (contact: Omit<TrustedContact, 'id'>) => void;
}

const AddContactDialog: React.FC<AddContactDialogProps> = ({
  open,
  onClose,
  onSave,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Omit<TrustedContact, 'id'>>();

  React.useEffect(() => {
    if (!open) {
      reset();
    }
  }, [open, reset]);

  const onSubmit = (data: Omit<TrustedContact, 'id'>) => {
    onSave(data);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle>Add Trusted Contact</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            This person will ONLY be contacted if you don't respond to activity 
            checks for your specified period. They won't know they've been added 
            as a trusted contact until/unless that happens.
          </Alert>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Full Name"
                {...register('name', { required: 'Name is required' })}
                error={!!errors.name}
                helperText={errors.name?.message}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Email Address"
                type="email"
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
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Phone Number (Optional)"
                {...register('phoneNumber', {
                  pattern: {
                    value: /^\+?[1-9]\d{1,14}$/,
                    message: 'Invalid phone number',
                  },
                })}
                error={!!errors.phoneNumber}
                helperText={errors.phoneNumber?.message || 'Include country code (e.g., +1234567890)'}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Address (Optional)"
                multiline
                rows={3}
                {...register('address')}
                error={!!errors.address}
                helperText={errors.address?.message || 'Physical mailing address'}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Relationship"
                {...register('relationship', { required: 'Relationship is required' })}
                error={!!errors.relationship}
                helperText={errors.relationship?.message || 'e.g., Spouse, Child, Sibling, Friend'}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">
            Add Contact
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default Contacts;