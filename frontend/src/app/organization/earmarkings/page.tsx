'use client';

import { useState, useEffect } from 'react';
import EarmarkingManager from '@/components/organization/EarmarkingManager';
import { Earmarking } from '@/types/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

async function fetchEarmarkings(orgId: string): Promise<Earmarking[]> {
  try {
    const response = await fetch(`${API_URL}/organisation/${orgId}/earmarking/list`);

    if (!response.ok) {
      throw new Error('Failed to fetch earmarkings');
    }

    return response.json();
  } catch (error) {
    console.error('Failed to fetch earmarkings:', error);
    return [];
  }
}

export default function EarmarkingsPage() {
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([]);
  const organizationId = '591671920';

  useEffect(() => {
    fetchEarmarkings(organizationId).then(setEarmarkings);
  }, [organizationId]);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Earmarkings</h1>
      <EarmarkingManager
        initialEarmarkings={earmarkings}
        organizationId={organizationId}
      />
    </div>
  );
}
