'use client';

import { useState, useEffect } from 'react';
import EarmarkingManager from '@/components/organization/EarmarkingManager';
import { Earmarking } from '@/types/types';
import { fetchEarmarkings } from './earmarkings';
import withAuth from '../api/RequiresAuth';


function EarmarkingsPage() {
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

export default withAuth(EarmarkingsPage)
