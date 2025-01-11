'use client';

import { useState, useEffect } from 'react';
import DonationsList from '@/components/organization/DonationsList';
import { Donation } from '@/types/types';
import axiosInstance from '../api/axiosInstance';
import axios from 'axios';

async function fetchDonations(orgId: string): Promise<Donation[]> {
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
