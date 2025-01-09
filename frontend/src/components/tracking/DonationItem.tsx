'use client'

import { Donation } from '../../types/types';
import { useTheme } from '../../contexts/ThemeContext';
import { TransitSchematic } from './TransitSchematic';
import { dateFormatter } from '@/lib/utils';

interface DonationItemProps {
  donation: Donation;
  onClick: () => void;
}

export const DonationItem: React.FC<DonationItemProps> = ({ donation, onClick }) => {
  const { theme } = useTheme();
  const latestStatus = donation.status[donation.status.length - 1];

  console.log(latestStatus);

  return (
    <div 
      className={`${theme.card} rounded-lg shadow-lg p-4 mb-4 cursor-pointer transition-all duration-300 ease-in-out hover:shadow-xl`}
      onClick={onClick}
    >
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div className="flex-grow">
          <div className="flex items-center justify-between sm:justify-start">
            <h3 className={`text-xl font-semibold ${theme.text} mr-4`}>{donation.organisation}</h3>
            <span className={`text-sm ${theme.textLight}`}>{dateFormatter.format(new Date(donation.date))}</span>
          </div>
          <p className={`${theme.textLight} mt-1`}>{donation.earmarking}</p>
          <div className="flex items-center mt-2">
            <span className={`text-base font-bold ${theme.text} mr-4`}>{donation.amount.amount} Euro</span>
            <span className={`text-sm ${theme.statusColors[latestStatus.status.toLowerCase() as keyof typeof theme.statusColors]} px-2 py-1 rounded-full text-white`}>
              {latestStatus.status}
            </span>
          </div>
        </div>
        <div className="mt-3 sm:mt-0 sm:ml-4">
          <TransitSchematic status={donation.status} />
          <p className={`text-sm ${theme.textLight} mt-1`}>Last Updated: {dateFormatter.format(new Date(latestStatus.date))}</p>
        </div>
      </div>
    </div>
  );
};

