'use client';

import { DonationWithDonor, Money } from '@/types/types';
import axiosInstance from '../api/axiosInstance';
import axios from 'axios';

export async function fetchDonations(
  orgId: string,
): Promise<DonationWithDonor[]> {
  try {
    const response = await axiosInstance.get(`/donate/${orgId}/donations`);
    return response.data.donations;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      // Axios-specific error handling
      console.error(
        'Failed to fetch donations:',
        error.response?.status,
        error.response?.statusText,
      );
    } else {
      // Non-Axios error handling
      console.error('Failed to fetch donations:', error);
    }
    return [];
  }
}

export async function createDonation(
  orgId: string,
  donorName: string,
  donorEmail: string,
  amount: Money,
  earmarking: string | null,
  accountName: string,
) {
  try {
    const response = await axiosInstance.post(
      `/donate/${orgId}/account/${accountName}`,
      {
        donorName,
        donorEmail,
        amount,
        earmarking,
      },
    );

    if (response.status !== 200) {
      throw new Error('Failed to create donation');
    }

    return { success: true };
  } catch (error) {
    return {
      success: false,
      error:
        error instanceof Error ? error.message : 'Failed to create donation',
    };
  }
}
