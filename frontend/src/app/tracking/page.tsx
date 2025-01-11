'use client';

import { useState, useEffect } from 'react';
import { themes } from '@/styles/themes';
import { getTheme } from '@/contexts/ThemeContext';
import { getDonations } from './donations';
import { TrackingPageClient } from '@/components/tracking/TrackingPage';
import { Donations } from '@/types/types';

const organizationId = '591671920';

export default function Tracking({
  searchParams,
}: {
  searchParams: { id: string };
}) {
  const [donations, setDonations] = useState<Donations | null>(null);
  const [theme, setTheme] = useState(themes.default);

  useEffect(() => {
    const fetchData = async () => {
      const [donationsData, themeData] = await Promise.all([
        getDonations(searchParams.id),
        getTheme(organizationId),
      ]);
      setDonations(donationsData);
      setTheme(themeData || themes.default);
    };

    fetchData();
  }, [searchParams.id]);

  if (!donations) {
    return <div>No donations found</div>;
  }

  return <TrackingPageClient donations={donations} theme={theme} />;
}
