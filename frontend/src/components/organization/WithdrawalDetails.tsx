'use client';

import { Withdrawal } from '@/types/types';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog';
import { useTheme } from '@/contexts/ThemeContext';

interface WithdrawalDetailsProps {
  withdrawal: Withdrawal;
  onClose: () => void;
}

export const WithdrawalDetails: React.FC<WithdrawalDetailsProps> = ({
  withdrawal,
  onClose,
}) => {
  const { theme } = useTheme();
  const date = new Date(withdrawal.time).toLocaleString();

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className={theme.card}>
        <DialogHeader>
          <DialogTitle className={theme.text}>Withdrawal Details</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="flex flex-col space-y-2">
            <span className={`font-semibold ${theme.text}`}>Description</span>
            <span className={theme.text}>{withdrawal.description}</span>
          </div>
          <div className="flex flex-col space-y-2">
            <span className={`font-semibold ${theme.text}`}>Amount</span>
            <span className={theme.text}>€{withdrawal.amount.amount}</span>
          </div>
          <div className="flex flex-col space-y-2">
            <span className={`font-semibold ${theme.text}`}>From Account</span>
            <span className={theme.text}>{withdrawal.fromAccount}</span>
          </div>
          {withdrawal.earmarking && (
            <div className="flex flex-col space-y-2">
              <span className={`font-semibold ${theme.text}`}>Earmarking</span>
              <span className={theme.text}>{withdrawal.earmarking}</span>
            </div>
          )}
          <div className="flex flex-col space-y-2">
            <span className={`font-semibold ${theme.text}`}>Date</span>
            <span className={theme.text}>{date}</span>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};
