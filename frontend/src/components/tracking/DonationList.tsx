'use client';

import { useState } from 'react';
import { Donation } from '../../types/types';
import { DonationItem } from './DonationItem';
import { DonationDetails } from './DonationDetails';
import { useTheme } from '../../contexts/ThemeContext';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../organization/ui/select';

type SortOption = 'amount' | 'newest' | 'oldest' | 'lastUpdated';

interface DonationListProps {
  donations: Donation[];
}

export const DonationList: React.FC<DonationListProps> = ({ donations }) => {
  const [selectedDonation, setSelectedDonation] = useState<Donation | null>(
    null,
  );
  const [sortBy, setSortBy] = useState<SortOption>('newest');
  const { theme } = useTheme();

  const sortDonations = (donations: Donation[]): Donation[] => {
    const sortedDonations = [...donations];
    switch (sortBy) {
      case 'amount':
        return sortedDonations.sort(
          (a, b) => parseFloat(b.amount.amount) - parseFloat(a.amount.amount),
        );
      case 'newest':
        return sortedDonations.sort(
          (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime(),
        );
      case 'oldest':
        return sortedDonations.sort(
          (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
        );
      case 'lastUpdated':
        return sortedDonations.sort((a, b) => {
          const lastStatusA = a.status[a.status.length - 1];
          const lastStatusB = b.status[b.status.length - 1];
          return (
            new Date(lastStatusB.date).getTime() -
            new Date(lastStatusA.date).getTime()
          );
        });
      default:
        return sortedDonations;
    }
  };

  const sortedDonations = sortDonations(donations);

  return (
    <div className={theme.font}>
      <div className="flex justify-between items-center mb-4">
        <h2 className={`text-2xl font-bold ${theme.text}`}>Your Donations</h2>
        <Select
          value={sortBy}
          onValueChange={(value) => setSortBy(value as SortOption)}
        >
          <SelectTrigger className="w-[180px] bg-white shadow-md rounded-lg border border-gray-200">
            <SelectValue placeholder="Sort by" />
          </SelectTrigger>
          <SelectContent className="bg-white">
            <SelectItem value="newest">Newest First</SelectItem>
            <SelectItem value="oldest">Oldest First</SelectItem>
            <SelectItem value="amount">Highest Amount</SelectItem>
            <SelectItem value="lastUpdated">Last Updated</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className="space-y-4">
        {sortedDonations.map((donation) => (
          <DonationItem
            key={donation.donationId}
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
