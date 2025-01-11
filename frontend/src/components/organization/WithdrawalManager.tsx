'use client';

import { useState } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Textarea } from './ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from './ui/select';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { BankAccount, Earmarking } from '@/types/types';
import { createWithdrawal } from '@/app/organization/transactions/transactions';

interface WithdrawalManagerProps {
  accounts: BankAccount[];
  earmarkings: Earmarking[];
  organizationId: string;
}

export default function WithdrawalManager({
  accounts,
  earmarkings,
  organizationId,
}: WithdrawalManagerProps) {
  const [amount, setAmount] = useState('');
  const [fromAccount, setFromAccount] = useState('');
  const [earmarking, setEarmarking] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const createNewWithdrawal = async () => {
    if (!amount || !fromAccount || !description) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      const result = await createWithdrawal(
        organizationId,
        fromAccount,
        earmarking,
        { amount: amount },
        description,
      );

      if (result.success) {
        setAmount('');
        setFromAccount('');
        setEarmarking('');
        setDescription('');
        setSuccessMessage('Withdrawal created successfully!');
        setError('');
        setTimeout(() => setSuccessMessage(''), 3000);
      } else {
        setError(result.error || 'Failed to create withdrawal');
      }
    } catch {
      setError('An error occurred while creating the withdrawal');
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>New Withdrawal</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 gap-4">
          <Input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Amount"
            step="0.01"
          />

          <Select onValueChange={setFromAccount}>
            <SelectTrigger>
              <SelectValue placeholder="From account" />
            </SelectTrigger>
            <SelectContent>
              {accounts.map((account) => (
                <SelectItem key={account.name} value={account.name}>
                  {account.name} ({account.balance.amount})
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select onValueChange={setEarmarking}>
            <SelectTrigger>
              <SelectValue placeholder="Earmarking (optional)" />
            </SelectTrigger>
            <SelectContent>
              {earmarkings.map((earmarking) => (
                <SelectItem key={earmarking.name} value={earmarking.name}>
                  {earmarking.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Description"
          />

          {error && <div className="text-red-500">{error}</div>}
          {successMessage && (
            <div className="text-green-500">{successMessage}</div>
          )}

          <Button onClick={createNewWithdrawal}>Create Withdrawal</Button>
        </div>
      </CardContent>
    </Card>
  );
}
