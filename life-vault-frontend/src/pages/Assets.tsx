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
  MenuItem,
  Grid,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../services/api';
import { Asset, AssetType } from '../types';

const assetTypeLabels: Record<AssetType, string> = {
  [AssetType.BANK_ACCOUNT]: 'Bank Account',
  [AssetType.INVESTMENT]: 'Investment',
  [AssetType.REAL_ESTATE]: 'Real Estate',
  [AssetType.INSURANCE]: 'Insurance',
  [AssetType.CRYPTO]: 'Cryptocurrency',
  [AssetType.RETIREMENT]: 'Retirement Account',
  [AssetType.BUSINESS]: 'Business',
  [AssetType.VEHICLE]: 'Vehicle',
  [AssetType.VALUABLE]: 'Valuable Item',
  [AssetType.OTHER]: 'Other',
};

const Assets: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [editingAsset, setEditingAsset] = useState<Asset | null>(null);
  const queryClient = useQueryClient();

  const { data: assets = [], isLoading } = useQuery<Asset[]>(
    'assets',
    async () => {
      const response = await api.get('/assets');
      return response.data;
    }
  );

  const createMutation = useMutation(
    (asset: Asset) => api.post('/assets', asset),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('assets');
        toast.success('Asset created successfully');
        handleCloseDialog();
      },
    }
  );

  const updateMutation = useMutation(
    ({ id, asset }: { id: number; asset: Asset }) =>
      api.put(`/assets/${id}`, asset),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('assets');
        toast.success('Asset updated successfully');
        handleCloseDialog();
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => api.delete(`/assets/${id}`),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('assets');
        toast.success('Asset deleted successfully');
      },
    }
  );

  const handleOpenDialog = (asset?: Asset) => {
    setEditingAsset(asset || null);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingAsset(null);
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this asset?')) {
      deleteMutation.mutate(id);
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Assets</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Add Asset
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Institution</TableCell>
              <TableCell>Location</TableCell>
              <TableCell>Description</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  Loading...
                </TableCell>
              </TableRow>
            ) : assets.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No assets found. Add your first asset to get started.
                </TableCell>
              </TableRow>
            ) : (
              assets.map((asset) => (
                <TableRow key={asset.id}>
                  <TableCell>{asset.name}</TableCell>
                  <TableCell>
                    <Chip
                      label={assetTypeLabels[asset.type]}
                      size="small"
                      color="primary"
                    />
                  </TableCell>
                  <TableCell>{asset.institution || '-'}</TableCell>
                  <TableCell>{asset.location || '-'}</TableCell>
                  <TableCell>{asset.description || '-'}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleOpenDialog(asset)}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handleDelete(asset.id!)}
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

      <AssetDialog
        open={openDialog}
        onClose={handleCloseDialog}
        asset={editingAsset}
        onSave={(asset) => {
          if (editingAsset?.id) {
            updateMutation.mutate({ id: editingAsset.id, asset });
          } else {
            createMutation.mutate(asset);
          }
        }}
      />
    </Box>
  );
};

interface AssetDialogProps {
  open: boolean;
  onClose: () => void;
  asset: Asset | null;
  onSave: (asset: Asset) => void;
}

const AssetDialog: React.FC<AssetDialogProps> = ({
  open,
  onClose,
  asset,
  onSave,
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Asset>({
    defaultValues: asset || {
      type: AssetType.BANK_ACCOUNT,
    },
  });

  React.useEffect(() => {
    reset(asset || { type: AssetType.BANK_ACCOUNT });
  }, [asset, reset]);

  const onSubmit = (data: Asset) => {
    onSave(data);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle>{asset ? 'Edit Asset' : 'Add New Asset'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Asset Name"
                {...register('name', { required: 'Name is required' })}
                error={!!errors.name}
                helperText={errors.name?.message}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                select
                label="Asset Type"
                defaultValue={asset?.type || AssetType.BANK_ACCOUNT}
                {...register('type', { required: 'Type is required' })}
                error={!!errors.type}
                helperText={errors.type?.message}
              >
                {Object.entries(assetTypeLabels).map(([value, label]) => (
                  <MenuItem key={value} value={value}>
                    {label}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Institution (Optional)"
                {...register('institution')}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Location (Optional)"
                {...register('location')}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Description (Optional)"
                {...register('description')}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Private Notes (Optional)"
                {...register('notes')}
                helperText="These notes are encrypted and only visible to you"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained">
            {asset ? 'Update' : 'Add'} Asset
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default Assets;