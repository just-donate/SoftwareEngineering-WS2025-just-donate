'use client'

import { useState } from 'react'
import { Donation } from '@/types/types'
import { useTheme } from '@/contexts/ThemeContext'
import { TransitSchematic } from './TransitSchematic'
import { Heart } from 'lucide-react'

interface DonationItemProps {
  donation: Donation
  onClick: () => void
}

const ThankYouMessage = () => (
    <div className="absolute inset-0 flex items-center justify-center bg-white bg-opacity-90 z-10 animate-fade-in">
      <div className="text-center animate-pop-up">
        <Heart className="w-12 h-12 text-red-500 mx-auto mb-2" />
        <p className="text-lg font-semibold text-gray-800">Thank you for your donation!</p>
      </div>
    </div>
)

export const DonationItem: React.FC<DonationItemProps> = ({ donation, onClick }) => {
  const { theme } = useTheme()
  const [showThankYou, setShowThankYou] = useState(false)
  const latestStatus = donation.status[donation.status.length - 1]

  const handleClick = () => {
    setShowThankYou(true)
    setTimeout(() => {
      setShowThankYou(false)
      onClick()
    }, 1500)
  }

  return (
      <div
          className={`${theme.card} rounded-lg shadow-lg p-4 mb-4 cursor-pointer transition-all duration-300 ease-in-out hover:shadow-xl relative`}
          onClick={handleClick}
      >
        {showThankYou && <ThankYouMessage />}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div className="flex-grow">
            <div className="flex items-center justify-between sm:justify-start">
              <h3 className={`text-xl font-semibold ${theme.text} mr-4`}>{donation.ngo}</h3>
              <span className={`text-sm ${theme.textLight}`}>{donation.date}</span>
            </div>
            <p className={`${theme.textLight} mt-1`}>{donation.project}</p>
            <div className="flex items-center mt-2">
              <span className={`text-base font-bold ${theme.text} mr-4`}>{donation.amount} {donation.currency}</span>
              <span className={`text-sm ${theme.statusColors[latestStatus.status.toLowerCase() as keyof typeof theme.statusColors]} px-2 py-1 rounded-full text-white`}>
              {latestStatus.status}
            </span>
            </div>
          </div>
          <div className="mt-3 sm:mt-0 sm:ml-4">
            <TransitSchematic status={donation.status} />
            <p className={`text-sm ${theme.textLight} mt-1`}>Last Updated: {latestStatus.date}</p>
          </div>
        </div>
      </div>
  )
}

