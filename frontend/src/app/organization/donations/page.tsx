'use client';

import { useState, useEffect } from 'react';
import DonationsList from '@/components/organization/DonationsList';
import { Donation } from '@/types/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

async function fetchDonations(orgId: string): Promise<Donation[]> {
  try {
    const response = await fetch(`${API_URL}/donate/${orgId}/donations`);

    if (!response.ok) {
      console.error(
        'Failed to fetch donations:',
        response.status,
        response.statusText,
      );
      throw new Error('Failed to fetch donations');
    }

    const data = await response.json();
    return data.donations;
  } catch (error) {
    console.error('Failed to fetch donations:', error);
    return [];
  }
}

export default function DonationsPage() {
  const [donations, setDonations] = useState<Donation[]>([]);
  const organizationId = '591671920';

  useEffect(() => {
    fetchDonations(organizationId).then(setDonations);
  }, [organizationId]);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Donations</h1>
      <DonationsList initialDonations={donations} />
    </div>
  );
}
