'use client';

import { Money, Transaction } from '@/types/types';
import axiosInstance from '../api/axiosInstance';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export async function fetchTransactions(orgId: string): Promise<Transaction[]> {
  try {
    const response = await axiosInstance.get(
      `/organisation/${orgId}/transaction/list`,
    );

    // Axios automatically parses JSON, so no need for `response.json()`
    return response.data;
  } catch (error) {
    console.error('Failed to fetch transactions:', error);
    return []; // Return an empty array if the request fails
  }
}

export async function createTransfer(
  orgId: string,
  fromAccount: string,
  toAccount: string,
  amount: {
    amount: string;
  },
): Promise<{ success: boolean; error?: string }> {
  try {
    // Use axiosInstance to make the POST request
    await axiosInstance.post(`/transfer/${orgId}`, {
      fromAccount,
      toAccount,
      amount,
    });

    return { success: true }; // Return success if no errors occur
  } catch (error) {
    console.error('Failed to create transfer:', error);

    return {
      success: false,
      error:
        error instanceof Error ? error.message : 'Failed to create transfer',
    };
  }
}

export async function createWithdrawal(
  orgId: string,
  fromAccount: string,
  earmarking: string,
  amount: Money,
  description: string,
): Promise<{ success: boolean; error?: string }> {
  try {
    // Use axiosInstance to make the POST request
    await axiosInstance.post(`/withdraw/${orgId}`, {
      fromAccount,
      earmarking,
      amount,
      description,
    });

    return { success: true }; // Return success if no errors occur
  } catch (error) {
    console.error('Failed to create withdrawal:', error);

    return {
      success: false,
      error:
        error instanceof Error ? error.message : 'Failed to create withdrawal',
    };
  }
}
