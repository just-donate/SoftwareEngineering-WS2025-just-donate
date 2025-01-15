'use client';

import { useState } from 'react';
import { Withdrawal } from '@/types/types';
import { WithdrawalItem } from './WithdrawalItem';
import { WithdrawalDetails } from './WithdrawalDetails';
import { useTheme } from '@/contexts/ThemeContext';

interface WithdrawalListProps {
  withdrawals: Withdrawal[];
}

export const WithdrawalList: React.FC<WithdrawalListProps> = ({
  withdrawals,
}) => {
  const [selectedWithdrawal, setSelectedWithdrawal] =
    useState<Withdrawal | null>(null);
  const { theme } = useTheme();

  return (
    <div className={theme.font}>
      <h2 className={`text-2xl font-bold mb-4 ${theme.text}`}>Withdrawals</h2>
      <div className="space-y-4">
        {withdrawals.map((withdrawal) => (
          <WithdrawalItem
            key={withdrawal.id}
            withdrawal={withdrawal}
            onClick={() => setSelectedWithdrawal(withdrawal)}
          />
        ))}
      </div>
      {selectedWithdrawal && (
        <WithdrawalDetails
          withdrawal={selectedWithdrawal}
          onClose={() => setSelectedWithdrawal(null)}
        />
      )}
    </div>
  );
};
