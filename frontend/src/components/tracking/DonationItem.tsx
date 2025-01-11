'use client';

import { useState, useEffect } from 'react'
import { Donation } from '@/types/types'
import { useTheme } from '@/contexts/ThemeContext'
import { TransitSchematic } from './TransitSchematic'
import { Heart } from 'lucide-react'
import Cookies from 'js-cookie'
import { dateFormatter } from '@/lib/utils'

interface DonationItemProps {
  donation: Donation
  onClick: () => void
}

export const DonationItem: React.FC<DonationItemProps> = ({ donation, onClick }) => {
  const { theme } = useTheme()
  const [showThankYou, setShowThankYou] = useState(false)
  const [isExiting, setIsExiting] = useState(false)
  const latestStatus = donation.status[donation.status.length - 1]

  useEffect(() => {
    const shownDonations = Cookies.get('shownDonations')
    const shownDonationIds = shownDonations ? JSON.parse(shownDonations) : []
    
    if (!shownDonationIds.includes(donation.donationId)) {
      setShowThankYou(true)
      const updatedShownDonations = [...shownDonationIds, donation.donationId]
      Cookies.set('shownDonations', JSON.stringify(updatedShownDonations), { expires: 365 })
      
      setTimeout(() => {
        setIsExiting(true)
        setTimeout(() => {
          setShowThankYou(false)
          setIsExiting(false)
        }, 500)
      }, 2000)
    }
  }, [donation.donationId])

  return (
    <div
      className={`${theme.card} rounded-lg shadow-lg p-4 mb-4 cursor-pointer transition-all duration-300 ease-in-out hover:shadow-xl relative`}
      onClick={onClick}
    >
      {showThankYou && (
        <div className={`absolute rounded-lg inset-0 flex items-center justify-center bg-white bg-opacity-90 z-10 transition-opacity duration-500 ${isExiting ? 'opacity-0' : 'opacity-100'}`}>
          <div className={`text-center transition-transform duration-500 ${isExiting ? 'scale-95 opacity-0' : 'scale-100 opacity-100'}`}>
            <Heart className="w-12 h-12 text-red-500 mx-auto mb-2" />
            <p className="text-lg font-semibold text-gray-800">Thank you for your donation!</p>
          </div>
        </div>
      )}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div className="flex-grow">
          <div className="flex items-center justify-between sm:justify-start">
            <h3 className={`text-xl font-semibold ${theme.text} mr-4`}>{donation.organisation}</h3>
            <span className={`text-sm ${theme.textLight}`}>
              {dateFormatter.format(new Date(donation.date))}
            </span>
          </div>
          <p className={`${theme.textLight} mt-1`}>{donation.earmarking}</p>
          <div className="flex items-center mt-2">
            <span className={`text-base font-bold ${theme.text} mr-4`}>
              {donation.amount.amount} Euro
            </span>
            <span
              className={`text-sm ${theme.statusColors[latestStatus.status.toLowerCase() as keyof typeof theme.statusColors]} px-2 py-1 rounded-full text-white`}
            >
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
