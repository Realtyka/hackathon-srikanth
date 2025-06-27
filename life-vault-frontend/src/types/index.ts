export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface SignupData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

export interface Asset {
  id?: number;
  name: string;
  description?: string;
  type: AssetType;
  institution?: string;
  location?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  isActive?: boolean;
}

export enum AssetType {
  BANK_ACCOUNT = 'BANK_ACCOUNT',
  INVESTMENT = 'INVESTMENT',
  REAL_ESTATE = 'REAL_ESTATE',
  INSURANCE = 'INSURANCE',
  CRYPTO = 'CRYPTO',
  RETIREMENT = 'RETIREMENT',
  BUSINESS = 'BUSINESS',
  VEHICLE = 'VEHICLE',
  VALUABLE = 'VALUABLE',
  OTHER = 'OTHER',
}

export interface TrustedContact {
  id?: number;
  name: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  relationship: string;
  isVerified?: boolean;
  verifiedAt?: string;
  isNotified?: boolean;
  notifiedAt?: string;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  firstName: string;
  lastName: string;
}