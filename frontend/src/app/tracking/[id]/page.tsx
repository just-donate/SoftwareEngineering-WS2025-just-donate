'use client'

import { useEffect } from 'react';
import { Donation } from '@/types/types';
import { useTheme } from '../../../contexts/ThemeContext'
import { getDonations } from './donations';
import { useState } from 'react';
import { use } from 'react';
import { Navigation } from '@/components/tracking/Navigation';
import { DonationList } from '@/components/tracking/DonationList';


export default function TrackingPage({
  params
}: {
  params: Promise<{ id: string }>
}) {

  const { theme } = useTheme();
  const [donations, setDonations] = useState<Donation[] | null>(null);
  const resolvedParams = use(params);

  useEffect(() => {
    const donations = getDonations(resolvedParams.id)
    setDonations(donations)
  }, [resolvedParams.id])

  return (
    <div className={`min-h-screen ${theme.background}`}>
      <Navigation links={[{ link: '/help', name: 'Help'}]} />
      <main className={`${theme.text}`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          {donations ? (
            <DonationList donations={donations} />
          ) : (
            <div className="text-center py-12">
              <h2 className="text-2xl font-semibold">No donations found</h2>
              <p className="mt-2">The tracking ID you provided could not be found.</p>
            </div>
          )}
        </div>
      </main>
    </div>
  )
}


