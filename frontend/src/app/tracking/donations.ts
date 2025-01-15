'use client';

import { Donations } from '@/types/types';
import axiosInstance from '../organization/api/axiosInstance';
import { config } from '@/lib/config';

const organizationId = config.organizationId;

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
