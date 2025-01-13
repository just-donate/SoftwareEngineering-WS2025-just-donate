'use client';

import { useState, useEffect } from 'react';
import { Donation } from '@/types/types';
import { fetchDonations } from './donations';
import DonationManager from '@/components/organization/DonationManager';
import withAuth from '../api/RequiresAuth';

function DonationsPage() {
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

export default withAuth(DonationsPage);
