import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { render } from '../../test/test-utils';
import Contacts from '../Contacts';
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

describe('Contacts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders contacts list', async () => {
    const mockContacts = [
      { id: 1, name: 'John Doe', email: 'john@example.com', phoneNumber: '+1234567890', address: '123 Main St', relationship: 'Friend' },
      { id: 2, name: 'Jane Smith', email: 'jane@example.com', phoneNumber: '+0987654321', address: '456 Oak Ave', relationship: 'Family' },
    ];

    vi.mocked(api.get).mockResolvedValueOnce({ data: mockContacts });

    render(<Contacts />);

    expect(await screen.findByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('jane@example.com')).toBeInTheDocument();
  });

  it('shows empty state when no contacts', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] });

    render(<Contacts />);

    expect(await screen.findByText(/no trusted contacts added yet/i)).toBeInTheDocument();
  });

  it('displays privacy notice about contacts', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] });

    render(<Contacts />);

    await waitFor(() => {
      expect(screen.getByText(/will not be notified when you add them/i)).toBeInTheDocument();
    });
  });
});