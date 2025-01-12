'use client';

import { useState, useEffect } from 'react';
import DonationsList from '@/components/organization/DonationsList';
import { Donation } from '@/types/types';
import { Input } from '@/components/organization/ui/input';
import { Button } from '@/components/organization/ui/button';
import axiosInstance from '../api/axiosInstance';
import { fetchDonations } from './donations';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/organization/ui/card';
import DonationManager from '@/components/organization/DonationManager';

export default function DonationsPage() {
  const [donations, setDonations] = useState<Donation[]>([]);

  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  useEffect(() => {
    fetchDonations(organizationId).then(setDonations);
  }, [organizationId]);


  return (
    <div>
      <DonationManager
        initialDonations={donations}
        organizationId={organizationId}
      />
    </div>
  );

}
