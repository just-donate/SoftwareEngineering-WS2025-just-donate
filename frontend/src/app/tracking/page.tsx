'use client';

import { themes } from '@/styles/themes';
import { getTheme } from '@/contexts/ThemeContext';
import { getDonations } from './donations';
import { TrackingPageClient } from '@/components/tracking/TrackingPage';
import { useEffect, useState } from 'react';
import { Donations } from '@/types/types';
import { useSearchParams } from 'next/navigation';

const organizationId = '591671920';

// @ts-nocheck
export default function Tracking() {
  const searchParams = useSearchParams();
  const id = searchParams.get('id') || '';

  const [donations, setDonations] = useState<Donations | null>(null);
  const [theme, setTheme] = useState(themes.default);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [donationsData, themeData] = await Promise.all([
          getDonations(id),
          getTheme(organizationId),
        ]);

        setDonations(donationsData);
        setTheme(themeData || themes.default);
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [id]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!donations) {
    return <div>No donations found</div>;
  }

  return <TrackingPageClient donations={donations.donations} theme={theme} />;
}
