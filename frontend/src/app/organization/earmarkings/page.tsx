'use client';

import { useState, useEffect } from 'react';
import EarmarkingManager from '@/components/organization/EarmarkingManager';
import { Earmarking } from '@/types/types';
import { fetchEarmarkings } from './earmarkings';
import withAuth from '../api/RequiresAuth';
import { config } from '@/lib/config';

function EarmarkingsPage() {
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([]);
  const organizationId = config.organizationId;

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

export default withAuth(EarmarkingsPage);
