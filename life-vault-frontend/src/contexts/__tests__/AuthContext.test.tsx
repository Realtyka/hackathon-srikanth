import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../AuthContext';
import { authService } from '../../services/authService';
import React from 'react';
import { BrowserRouter } from 'react-router-dom';

// Mock the auth service
vi.mock('../../services/authService');

// Mock react-router-dom navigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock react-toastify
vi.mock('react-toastify', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
  },
}));

describe('AuthContext', () => {
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <BrowserRouter>
      <AuthProvider>{children}</AuthProvider>
    </BrowserRouter>
  );

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('provides initial unauthenticated state', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });
    
    // Wait for loading to complete
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });
    
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('loads user from localStorage on mount', async () => {
    const mockToken = 'valid-jwt-token';
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    };

    localStorage.setItem('token', mockToken);
    localStorage.setItem('user', JSON.stringify(mockUser));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUser);
    });
  });

  it('handles login successfully', async () => {
    const mockLoginResponse = {
      token: 'new-jwt-token',
      type: 'Bearer',
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    };

    vi.mocked(authService.login).mockResolvedValueOnce(mockLoginResponse);

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login({ email: 'test@example.com', password: 'password123' });
    });

    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user).toEqual({
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    });
    expect(localStorage.getItem('token')).toBe('new-jwt-token');
    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('handles login failure', async () => {
    const errorMessage = 'Invalid credentials';
    vi.mocked(authService.login).mockRejectedValueOnce(new Error(errorMessage));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await expect(
      act(async () => {
        await result.current.login({ email: 'test@example.com', password: 'wrongpassword' });
      })
    ).rejects.toThrow(errorMessage);

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('handles signup successfully', async () => {
    vi.mocked(authService.signup).mockResolvedValueOnce(undefined);

    const { result } = renderHook(() => useAuth(), { wrapper });

    const signupData = {
      email: 'newuser@example.com',
      password: 'password123',
      firstName: 'New',
      lastName: 'User',
    };

    await act(async () => {
      await result.current.signup(signupData);
    });

    expect(authService.signup).toHaveBeenCalledWith(signupData);
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('handles logout', async () => {
    // Setup authenticated state
    const mockUser = {
      id: 1,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    };
    
    localStorage.setItem('token', 'existing-token');
    localStorage.setItem('user', JSON.stringify(mockUser));

    const { result } = renderHook(() => useAuth(), { wrapper });

    // Wait for initial load
    await waitFor(() => {
      expect(result.current.isAuthenticated).toBe(true);
    });

    // Mock authService.logout
    vi.mocked(authService.logout).mockImplementation(() => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    });

    // Logout
    act(() => {
      result.current.logout();
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('handles missing user data with token', async () => {
    const mockToken = 'valid-jwt-token';
    localStorage.setItem('token', mockToken);
    // No user data in localStorage

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
    });
  });
});