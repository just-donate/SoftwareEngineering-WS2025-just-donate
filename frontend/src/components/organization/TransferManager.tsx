'use client';

import { useState } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './ui/select';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { BankAccount } from '@/types/types';
import { createTransfer } from '@/app/organization/transactions/transactions';
import { useTheme } from '@/contexts/ThemeContext';

interface TransferManagerProps {
  accounts: BankAccount[];
  organizationId: string;
  onTransferSuccess?: () => void;
}

export default function TransferManager({
  accounts,
  organizationId,
  onTransferSuccess,
}: TransferManagerProps) {
  const { theme } = useTheme();
  const [amount, setAmount] = useState('');
  const [fromAccount, setFromAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const createNewTransfer = async () => {
    if (!amount || !fromAccount || !toAccount) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      const result = await createTransfer(
        organizationId,
        fromAccount,
        toAccount,
        { amount: amount },
      );

      if (result.success) {
        setAmount('');
        setFromAccount('');
        setToAccount('');
        setSuccessMessage('Transfer created successfully!');
        setError('');
        setTimeout(() => setSuccessMessage(''), 3000);
        onTransferSuccess?.();
      } else {
        setError(result.error || 'Failed to create transfer');
      }
    } catch {
      setError('An error occurred while creating the transfer');
    }
  };

  const isValidTransfer = (from: string, to: string): boolean => {
    const fromAcc = accounts.find((a) => a.name === from);
    const toAcc = accounts.find((a) => a.name === to);
    if (!fromAcc || !toAcc) return false;
    return true;
  };

  return (
    <Card className={theme.card}>
      <CardHeader>
        <CardTitle className={theme.text}>New Transfer</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 gap-4">
          <Input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Amount"
            step="0.01"
            className={`${theme.background} ${theme.text}`}
          />

          <Select onValueChange={setFromAccount}>
            <SelectTrigger className={`${theme.background} ${theme.text}`}>
              <SelectValue placeholder="From account" />
            </SelectTrigger>
            <SelectContent className={theme.card}>
              {accounts.map((account) => (
                <SelectItem key={account.name} value={account.name}>
                  {account.name} ({account.balance.amount})
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select onValueChange={setToAccount} disabled={!fromAccount}>
            <SelectTrigger className={`${theme.background} ${theme.text}`}>
              <SelectValue placeholder="To account" />
            </SelectTrigger>
            <SelectContent className={theme.card}>
              {accounts
                .filter(
                  (account) =>
                    account.name !== fromAccount &&
                    isValidTransfer(fromAccount, account.name),
                )
                .map((account) => (
                  <SelectItem key={account.name} value={account.name}>
                    {account.name} ({account.balance.amount})
                  </SelectItem>
                ))}
            </SelectContent>
          </Select>

          {error && <div className="text-red-500">{error}</div>}
          {successMessage && (
            <div className="text-green-500">{successMessage}</div>
          )}

          <Button onClick={createNewTransfer} className={theme.primary}>
            Create Transfer
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
