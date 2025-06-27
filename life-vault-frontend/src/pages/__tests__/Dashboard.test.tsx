import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { render } from '../../test/test-utils';
import Dashboard from '../Dashboard';
import api from '../../services/api';

// Mock the API
vi.mock('../../services/api');

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders dashboard with title', async () => {
    vi.mocked(api.get).mockImplementation((url) => {
      if (url === '/users/profile') return Promise.resolve({ data: { inactivityPeriodDays: 180 } });
      if (url === '/assets') return Promise.resolve({ data: [] });
      if (url === '/contacts') return Promise.resolve({ data: [] });
      return Promise.reject(new Error('Unknown URL'));
    });

    render(<Dashboard />);

    expect(await screen.findByText('Dashboard')).toBeInTheDocument();
  });

  it('displays user statistics', async () => {
    const mockAssets = [
      { id: 1, name: 'Asset 1' },
      { id: 2, name: 'Asset 2' },
    ];
    const mockContacts = [
      { id: 1, name: 'Contact 1' },
      { id: 2, name: 'Contact 2' },
      { id: 3, name: 'Contact 3' },
    ];

    vi.mocked(api.get).mockImplementation((url) => {
      if (url === '/users/profile') return Promise.resolve({ data: { inactivityPeriodDays: 180 } });
      if (url === '/assets') return Promise.resolve({ data: mockAssets });
      if (url === '/contacts') return Promise.resolve({ data: mockContacts });
      return Promise.reject(new Error('Unknown URL'));
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('Total Assets')).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument(); // Total assets
      expect(screen.getByText('Trusted Contacts')).toBeInTheDocument();
      expect(screen.getByText('3')).toBeInTheDocument(); // Total contacts
      expect(screen.getByText('Inactivity Period')).toBeInTheDocument();
      expect(screen.getByText('6 months')).toBeInTheDocument(); // Default period
    });
  });

  it('displays custom inactivity period', async () => {
    vi.mocked(api.get).mockImplementation((url) => {
      if (url === '/users/profile') return Promise.resolve({ data: { inactivityPeriodDays: 365 } });
      if (url === '/assets') return Promise.resolve({ data: [] });
      if (url === '/contacts') return Promise.resolve({ data: [] });
      return Promise.reject(new Error('Unknown URL'));
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByText('1 year')).toBeInTheDocument();
    });
  });

  it('displays correct icons for each stat', async () => {
    vi.mocked(api.get).mockImplementation((url) => {
      if (url === '/users/profile') return Promise.resolve({ data: { inactivityPeriodDays: 180 } });
      if (url === '/assets') return Promise.resolve({ data: [] });
      if (url === '/contacts') return Promise.resolve({ data: [] });
      return Promise.reject(new Error('Unknown URL'));
    });

    render(<Dashboard />);

    await waitFor(() => {
      expect(screen.getByTestId('AccountBalanceIcon')).toBeInTheDocument();
      expect(screen.getByTestId('PeopleIcon')).toBeInTheDocument();
      expect(screen.getByTestId('TimerIcon')).toBeInTheDocument();
    });
  });
});