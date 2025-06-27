import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { render } from '../../test/test-utils';
import Assets from '../Assets';
import api from '../../services/api';

// Mock the API
vi.mock('../../services/api');

// Mock react-toastify
vi.mock('react-toastify', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
  ToastContainer: () => null,
}));

describe('Assets', () => {
  const user = userEvent.setup();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders assets list', async () => {
    const mockAssets = [
      { id: 1, name: 'Checking Account', type: 'BANK_ACCOUNT', institution: 'Bank A' },
      { id: 2, name: 'Savings Account', type: 'BANK_ACCOUNT', institution: 'Bank B' },
    ];

    vi.mocked(api.get).mockResolvedValueOnce({ data: mockAssets });

    render(<Assets />);

    expect(await screen.findByText('Checking Account')).toBeInTheDocument();
    expect(screen.getByText('Savings Account')).toBeInTheDocument();
  });

  it('shows empty state when no assets', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] });

    render(<Assets />);

    expect(await screen.findByText(/no assets found/i)).toBeInTheDocument();
  });

  it('opens add asset dialog', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] });

    render(<Assets />);

    const addButton = await screen.findByRole('button', { name: /add asset/i });
    await user.click(addButton);

    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    vi.mocked(api.get).mockImplementation(() => 
      new Promise(() => {}) // Never resolves
    );

    render(<Assets />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
});