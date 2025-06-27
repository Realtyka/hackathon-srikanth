import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { render } from '../../test/test-utils';
import Settings from '../Settings';
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

describe('Settings', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays user profile data', async () => {
    const mockUser = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phoneNumber: '+1234567890',
      inactivityPeriodDays: 180
    };

    vi.mocked(api.get).mockResolvedValueOnce({ data: mockUser });

    render(<Settings />);

    await waitFor(() => {
      expect(screen.getByDisplayValue('John')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Doe')).toBeInTheDocument();
      expect(screen.getByDisplayValue('john@example.com')).toBeInTheDocument();
      expect(screen.getByDisplayValue('+1234567890')).toBeInTheDocument();
    });
  });

  it('shows loading state', () => {
    vi.mocked(api.get).mockImplementation(() => 
      new Promise(() => {}) // Never resolves
    );

    render(<Settings />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('handles API errors gracefully', async () => {
    vi.mocked(api.get).mockRejectedValueOnce(new Error('API Error'));

    render(<Settings />);

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      // Should still show the form structure
      expect(screen.getByText(/profile information/i)).toBeInTheDocument();
    });
  });
});