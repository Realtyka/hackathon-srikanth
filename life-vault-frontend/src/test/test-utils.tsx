import React from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from 'react-query';
import { AuthProvider } from '../contexts/AuthContext';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { ToastContainer } from 'react-toastify';

const theme = createTheme();

interface TestProvidersProps {
  children: React.ReactNode;
}

const TestProviders: React.FC<TestProvidersProps> = ({ children }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ThemeProvider theme={theme}>
          <AuthProvider>
            {children}
            <ToastContainer />
          </AuthProvider>
        </ThemeProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

const customRender = (
  ui: React.ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => render(ui, { wrapper: TestProviders, ...options });

export * from '@testing-library/react';
export { customRender as render };

// Mock data factories
export const createMockUser = (overrides = {}) => ({
  id: 1,
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  phoneNumber: '+1234567890',
  inactivityPeriodDays: 180,
  lastActivityAt: new Date().toISOString(),
  ...overrides,
});

export const createMockAsset = (overrides = {}) => ({
  id: 1,
  name: 'Test Bank Account',
  type: 'BANK_ACCOUNT',
  institutionName: 'Test Bank',
  accountIdentifier: '****1234',
  notes: 'Test notes',
  createdAt: new Date().toISOString(),
  ...overrides,
});

export const createMockContact = (overrides = {}) => ({
  id: 1,
  name: 'John Doe',
  email: 'john@example.com',
  phoneNumber: '+9876543210',
  relationship: 'Friend',
  isVerified: true,
  createdAt: new Date().toISOString(),
  ...overrides,
});