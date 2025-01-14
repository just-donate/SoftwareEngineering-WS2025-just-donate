'use client';

import { BankAccount } from '@/types/types';
import axiosInstance from '../api/axiosInstance';

export async function fetchBankAccounts(orgId: string): Promise<BankAccount[]> {
  try {
    const response = await axiosInstance.get<BankAccount[]>(
      `/organisation/${orgId}/account/list`,
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch bank accounts:', error);
    return [];
  }
}

export async function postBankAccount(
  orgId: string,
  name: string,
  amount: string,
) {
  try {
    const response = await axiosInstance.post(
      `/organisation/${orgId}/account`,
      {
        name,
        balance: {
          amount,
        },
      },
    );
    return response;
  } catch (error) {
    console.error('Failed to add bank account:', error);
    throw error;
  }
}
