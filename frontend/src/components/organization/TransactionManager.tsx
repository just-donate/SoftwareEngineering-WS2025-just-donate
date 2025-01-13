'use client';

import { useState, useEffect } from 'react';
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
import { Transaction, BankAccount, Earmarking } from '@/types/types';
import { createWithdrawal } from '@/app/organization/transactions/transactions';
import { createTransfer } from '@/app/organization/transactions/transactions';
import { useTheme } from '@/contexts/ThemeContext';

interface TransactionManagerProps {
  initialTransactions: Transaction[];
  accounts: BankAccount[];
  earmarkings: Earmarking[];
  organizationId: string;
}

export default function TransactionManager({
  initialTransactions,
  accounts,
  earmarkings,
  organizationId,
}: TransactionManagerProps) {
  const { theme } = useTheme();
  const [transactions, setTransactions] =
    useState<Transaction[]>(initialTransactions);
  const [amount, setAmount] = useState('');
  const [fromAccount, setFromAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [earmarking, setEarmarking] = useState('');
  const [transactionType, setTransactionType] = useState<
    'transfer' | 'withdrawal'
  >('transfer');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    setTransactions(initialTransactions);
  }, [initialTransactions]);

  const createTransaction = async () => {
    if (
      !amount ||
      !fromAccount ||
      (transactionType === 'transfer' ? !toAccount : !earmarking)
    ) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      const result =
        transactionType === 'transfer'
          ? await createTransfer(organizationId, fromAccount, toAccount, {
              amount,
            })
          : await createWithdrawal(
              organizationId,
              fromAccount,
              earmarking,
              { amount },
              Date.now().toString(),
            );

      if (!result.success) {
        throw new Error(result.error || `Failed to create ${transactionType}`);
      }

      // Optimistically update the UI
      const newTransaction: Transaction = {
        amount: parseFloat(amount),
        fromAccountId: fromAccount,
        toAccountId: transactionType === 'transfer' ? toAccount : null,
        earmarkingId: transactionType === 'withdrawal' ? earmarking : null,
        type: transactionType,
      };
      setTransactions([...transactions, newTransaction]);
      setAmount('');
      setFromAccount('');
      setToAccount('');
      setEarmarking('');
      setSuccessMessage('Transaction created successfully!');
      setError('');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : 'Failed to create transaction',
      );
      setSuccessMessage('');
    }
  };

  const isValidTransfer = (from: string, to: string): boolean => {
    const fromAcc = accounts.find((a) => a.name === from);
    const toAcc = accounts.find((a) => a.name === to);
    if (!fromAcc || !toAcc) return false;
    return true;
  };

  return (
    <div>
      <Card className={`mb-4 ${theme.card}`}>
        <CardHeader>
          <CardTitle className={theme.text}>New Transaction</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4">
            <Select
              onValueChange={(value: string) =>
                setTransactionType(value as 'transfer' | 'withdrawal')
              }
            >
              <SelectTrigger className={`${theme.background} ${theme.text}`}>
                <SelectValue placeholder="Transaction type" />
              </SelectTrigger>
              <SelectContent className={theme.card}>
                <SelectItem value="transfer">Transfer</SelectItem>
                <SelectItem value="withdrawal">Withdrawal</SelectItem>
              </SelectContent>
            </Select>

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

            {transactionType === 'transfer' ? (
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
            ) : (
              <Select onValueChange={setEarmarking}>
                <SelectTrigger className={`${theme.background} ${theme.text}`}>
                  <SelectValue placeholder="Earmarking" />
                </SelectTrigger>
                <SelectContent className={theme.card}>
                  {earmarkings.map((earmarking) => (
                    <SelectItem key={earmarking.name} value={earmarking.name}>
                      {earmarking.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}

            {error && <div className="text-red-500">{error}</div>}
            {successMessage && (
              <div className="text-green-500">{successMessage}</div>
            )}

            <Button onClick={createTransaction} className={theme.primary}>Create Transaction</Button>
          </div>
        </CardContent>
      </Card>

      <Card className={theme.card}>
        <CardHeader>
          <CardTitle className={theme.text}>Recent Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {transactions.map((transaction, index) => (
              <li key={index} className={`p-4 rounded-lg ${theme.secondary}`}>
                <span className={theme.text}>
                  {transaction.type === 'transfer'
                    ? `Transfer: ${transaction.amount} from ${accounts.find((a) => a.name === transaction.fromAccountId)?.name} to ${accounts.find((a) => a.name === transaction.toAccountId)?.name}`
                    : `Withdrawal: ${transaction.amount} from ${accounts.find((a) => a.name === transaction.fromAccountId)?.name} (${earmarkings.find((e) => e.name === transaction.earmarkingId)?.name})`}
                </span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
