'use client';

import { Earmarking } from '@/types/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function fetchEarmarkings(orgId: string): Promise<Earmarking[]> {
  try {
    const response = await fetch(
      `${API_URL}/organisation/${orgId}/earmarking/list`,
    );

    if (!response.ok) {
      throw new Error('Failed to fetch earmarkings');
    }

    return response.json();
  } catch (error) {
    console.error('Failed to fetch earmarkings:', error);
    return [];
  }
}
