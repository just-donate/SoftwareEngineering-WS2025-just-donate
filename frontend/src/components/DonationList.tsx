'use client'

import { useState } from 'react';
import { Donation } from '../types/types';
import { DonationItem } from './DonationItem';
import { DonationDetails } from './DonationDetails';
import { useTheme } from '../contexts/ThemeContext';

interface DonationListProps {
  donations: Donation[];
}

export const DonationList: React.FC<DonationListProps> = ({ donations }) => {
  const [selectedDonation, setSelectedDonation] = useState<Donation | null>(null);
  const { theme } = useTheme();

  return (
    <div className={theme.font}>
      <h2 className={`text-2xl font-bold mb-4 ${theme.text}`}>Your Donations</h2>
      <div className="space-y-4">
        {donations.map((donation) => (
          <DonationItem 
            key={donation.id} 
            donation={donation} 
            onClick={() => setSelectedDonation(donation)}
          />
        ))}
      </div>
      {selectedDonation && (
        <DonationDetails 
          donation={selectedDonation} 
          onClose={() => setSelectedDonation(null)}
        />
      )}
    </div>
  );
};

