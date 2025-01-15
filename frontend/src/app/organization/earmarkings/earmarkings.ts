'use client';

import { Earmarking } from '@/types/types';
import axiosInstance from '../api/axiosInstance';

export async function fetchEarmarkings(orgId: string): Promise<Earmarking[]> {
  try {
    const response = await axiosInstance.get(
      `public/organisation/${orgId}/earmarking/list`,
    );
    console.log(response.data);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch earmarkings:', error);
    return [];
  }
}
