'use client';

import { Donation } from '@/types/types';
import axiosInstance from '../api/axiosInstance';
import axios from 'axios';

export async function fetchDonations(orgId: string): Promise<Donation[]> {
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