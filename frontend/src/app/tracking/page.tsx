'use client'

import { useEffect, useState } from 'react'
import { getDonations } from './donations'
import { useSearchParams } from 'next/navigation'
import { Donation } from '@/types/types'
import { useTheme } from '../../contexts/ThemeContext'
import { Navigation } from '@/components/tracking/Navigation'
import { DonationList } from '@/components/tracking/DonationList'
import '../../styles/animations.css'

export default function TrackingPage() {
  const searchParams = useSearchParams()
  const trackingId = searchParams.get('id')

  const donations = getDonations(trackingId ?? '')

  const { theme } = useTheme()
  const [donationList, setDonationList] = useState<Donation[] | null>(donations)

  useEffect(() => {
    setDonationList(donations)
  }, [donations])

  return (
      <div className={`min-h-screen ${theme.background}`}>
        <Navigation links={[]} />
        <main className={`${theme.text}`}>
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            {donationList ? (
                <DonationList donations={donationList} />
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


