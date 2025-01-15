'use client';

import { Withdrawal } from '@/types/types';
import { Card, CardContent } from './ui/card';
import { useTheme } from '@/contexts/ThemeContext';

interface WithdrawalItemProps {
  withdrawal: Withdrawal;
  onClick: () => void;
}

export const WithdrawalItem: React.FC<WithdrawalItemProps> = ({
  withdrawal,
  onClick,
}) => {
  const { theme } = useTheme();
  const date = new Date(withdrawal.time).toLocaleDateString();

  return (
    <Card
      className={`${theme.card} cursor-pointer hover:opacity-80 transition-opacity`}
      onClick={onClick}
    >
      <CardContent className="pt-6">
        <div className="flex justify-between items-center">
          <div className="flex flex-col">
            <span className={`font-semibold ${theme.text}`}>
              {withdrawal.description}
            </span>
            <span className={`text-sm ${theme.text}`}>
              From: {withdrawal.fromAccount}
            </span>
            {withdrawal.earmarking && (
              <span className={`text-sm ${theme.text}`}>
                Earmarking: {withdrawal.earmarking}
              </span>
            )}
          </div>
          <div className="flex flex-col items-end">
            <span className={`font-bold ${theme.text}`}>
              €{withdrawal.amount.amount}
            </span>
            <span className={`text-sm ${theme.text}`}>{date}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}; 