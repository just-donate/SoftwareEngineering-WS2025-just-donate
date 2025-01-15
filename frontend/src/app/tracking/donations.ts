'use client';

import { Donations } from '@/types/types';
import axiosInstance from '../organization/api/axiosInstance';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

// TODO: Get the organization ID from the session/context
const organizationId = '591671920';

export async function getDonations(id: string): Promise<Donations | null> {
  try {
    const response = await axiosInstance.get(
      `/public/donate/${organizationId}/donor/${id}`,
    );

    return response.data;
  } catch (error) {
    console.error('Error fetching donations:', error);
    return null;
  }
}
