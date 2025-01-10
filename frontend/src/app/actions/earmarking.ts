'use server';

import { revalidatePath } from 'next/cache';
import { Earmarking } from '@/types/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export async function getEarmarkings(orgId: string): Promise<Earmarking[]> {
  try {
    const response = await fetch(
      `${API_URL}/organisation/${orgId}/earmarking/list`,
      {
        cache: 'no-store',
      },
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

export async function createEarmarking(orgId: string, name: string) {
  try {
    const response = await fetch(
      `${API_URL}/organisation/${orgId}/earmarking`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name }),
      },
    );

    if (!response.ok) {
      throw new Error('Failed to create earmarking');
    }

    revalidatePath('/organization/earmarkings');
    return { success: true };
  } catch (error) {
    return {
      success: false,
      error:
        error instanceof Error ? error.message : 'Failed to create earmarking',
    };
  }
}
