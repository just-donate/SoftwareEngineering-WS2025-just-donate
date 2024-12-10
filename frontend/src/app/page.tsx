'use client'

import { DonationList } from '../components/DonationList'
import { mockDonations } from '../data/mockDonations'
import { Navigation } from '../components/Navigation'
import { useTheme } from '../contexts/ThemeContext'

export default function Home() {
  const { theme } = useTheme();

  return (
    <div className={`min-h-screen ${theme.background}`}>
      <Navigation />
      <main className={`${theme.text}`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <DonationList donations={mockDonations} />
        </div>
      </main>
    </div>
  )
}

