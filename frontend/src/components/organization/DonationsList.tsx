'use client';

import { useState, useEffect } from 'react';
import { Input } from './ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './ui/select';
import DonationDetails from './DonationDetails';
import { DonationWithDonor } from '@/types/types';
import { useTheme } from '@/contexts/ThemeContext';

interface DonationsListProps {
  initialDonations: DonationWithDonor[];
}

export default function DonationsList({
  initialDonations,
}: DonationsListProps) {
  const { theme } = useTheme();
  const [donations, setDonations] =
    useState<DonationWithDonor[]>(initialDonations);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');

  useEffect(() => {
    setDonations(initialDonations);
  }, [initialDonations]);

  // Get unique status values from all donations
  const uniqueStatuses = Array.from(
    new Set(donations.flatMap((d) => d.status.map((s) => s.status))),
  );

  const filteredDonations = donations.filter((donation) => {
    const matchesSearch =
      (donation.donationId?.toLowerCase() || '').includes(
        searchTerm.toLowerCase(),
      ) ||
      (donation.date?.toLowerCase() || '').includes(searchTerm.toLowerCase()) ||
      donation.amount?.amount?.toString().includes(searchTerm) ||
      (donation.donor.name?.toLowerCase() || '').includes(
        searchTerm.toLowerCase(),
      ) ||
      (donation.donor.email?.toLowerCase() || '').includes(
        searchTerm.toLowerCase(),
      ) ||
      (donation.organisation?.toLowerCase() || '').includes(
        searchTerm.toLowerCase(),
      ) ||
      (donation.earmarking?.toLowerCase() || '').includes(
        searchTerm.toLowerCase(),
      );

    const matchesStatus =
      statusFilter === 'all' ||
      donation.status?.[donation.status.length - 1]?.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-4">
      <div className="flex gap-4">
        <Input
          placeholder="Search donations..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className={`max-w-sm ${theme.background} ${theme.text}`}
        />
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger
            className={`w-[180px] ${theme.background} ${theme.text}`}
          >
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent className={theme.card}>
            <SelectItem value="all">All Statuses</SelectItem>
            {uniqueStatuses.map((status) => (
              <SelectItem key={status} value={status}>
                {status}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-4">
        {filteredDonations.length === 0 ? (
          <p className={`text-center py-8 ${theme.textLight}`}>
            No donations found matching your criteria
          </p>
        ) : (
          filteredDonations.map((donation) => (
            <DonationDetails key={donation.donationId} donation={donation} />
          ))
        )}
      </div>
    </div>
  );
}
