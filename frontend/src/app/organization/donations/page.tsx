'use client';

import { useState, useEffect } from 'react';
import { DonationWithDonor } from '@/types/types';
import { fetchDonations } from './donations';
import DonationManager from '@/components/organization/DonationManager';
import withAuth from '../api/RequiresAuth';
import { config } from '@/lib/config';

function DonationsPage() {
  const [donations, setDonations] = useState<DonationWithDonor[]>([]);

  // TODO: Get the organization ID from the session/context
  const organizationId = config.organizationId;

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
