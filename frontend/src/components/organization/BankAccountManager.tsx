'use client';

import { useState, useEffect } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { BankAccount } from '@/types/types';
import {
  fetchBankAccounts,
  postBankAccount,
} from '@/app/organization/bank-accounts/bank-accounts';
import { useTheme } from '@/contexts/ThemeContext';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

interface BankAccountManagerProps {
  initialAccounts: BankAccount[];
  organizationId: string;
}

export default function BankAccountManager({
  initialAccounts,
  organizationId,
}: BankAccountManagerProps) {
  const { theme } = useTheme();
  const [accounts, setAccounts] = useState<BankAccount[]>(initialAccounts);

  useEffect(() => {
    setAccounts(initialAccounts);
  }, [initialAccounts]);

  const [newAccountName, setNewAccountName] = useState('');
  const [newAccountAmount, setNewAccountAmount] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const addBankAccount = async () => {
    const result = await postBankAccount(
      organizationId,
      newAccountName,
      newAccountAmount,
    );
    if (result.status === 200) {
      setSuccessMessage('Bank account added successfully');
      setError('');
      fetchBankAccounts(organizationId).then(setAccounts);
    }
  };

  return (
    <div>
      <Card className={`mb-4 ${theme.card}`}>
        <CardHeader>
          <CardTitle className={theme.text}>Add New Bank Account</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col space-y-2">
              <Input
                value={newAccountName}
                onChange={(e) => setNewAccountName(e.target.value)}
                placeholder="Account name"
                className={`mb-2 ${theme.background} ${theme.text}`}
              />
              <Input
                value={newAccountAmount}
                onChange={(e) => setNewAccountAmount(e.target.value)}
                placeholder="Initial balance"
                type="number"
                step="0.01"
                className={`${theme.background} ${theme.text}`}
              />
              <Button
                onClick={addBankAccount}
                className={`mt-2 ${theme.primary}`}
              >
                Add Account
              </Button>
            </div>
            {error && <div className="text-red-500">{error}</div>}
            {successMessage && (
              <div className="text-green-500">{successMessage}</div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card className={theme.card}>
        <CardHeader>
          <CardTitle className={theme.text}>Existing Bank Accounts</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full border-separate border-spacing-y-2">
              <thead>
                <tr>
                  <th className="text-left p-2">Accounts</th>
                  {accounts.length > 0 && accounts[0].byEarmarking.map(([earmarkingName]) => (
                    <th key={earmarkingName} className="text-center p-2">{earmarkingName}</th>
                  ))}
                  <th className="text-right p-2">Total Balance</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((account) => (
                  <tr key={account.name}>
                    <td className={`p-4 first:rounded-l-lg ${theme.secondary} ${theme.text}`}>{account.name}</td>
                    {account.byEarmarking.map(([earmarkingName, money]) => (
                      <td key={earmarkingName} className={`text-center p-4 ${theme.secondary} ${theme.text}`}>
                        {money.amount}
                      </td>
                    ))}
                    <td className={`text-right p-4 last:rounded-r-lg font-medium ${theme.secondary} ${theme.text}`}>
                      {account.balance.amount}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
