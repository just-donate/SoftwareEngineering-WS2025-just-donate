'use client';

import { BankAccount } from '@/types/types';
import axiosInstance from '../api/axiosInstance';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export async function fetchBankAccounts(orgId: string): Promise<BankAccount[]> {
  try {
    const response = await axiosInstance.get(`/organisation/${orgId}/account/list`);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch bank accounts:', error);
    return [];
  }
}
