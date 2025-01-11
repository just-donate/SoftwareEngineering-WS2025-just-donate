'use client';

import TransactionManager from '@/components/organization/TransactionManager';
import { fetchEarmarkings } from '../earmarkings/earmarkings';
import { fetchBankAccounts } from '../bank-accounts/bank-accounts';
import { fetchTransactions } from './transactions';
import { useEffect, useState } from 'react';
import { Transaction, BankAccount, Earmarking } from '@/types/types';
import withAuth from '../api/RequiresAuth';

function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [transactionsData, accountsData, earmarkingsData] =
          await Promise.all([
            fetchTransactions(organizationId),
            fetchBankAccounts(organizationId),
            fetchEarmarkings(organizationId),
          ]);

        setTransactions(transactionsData);
        setAccounts(accountsData);
        setEarmarkings(earmarkingsData);
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [organizationId]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Transactions</h1>
      <TransactionManager
        initialTransactions={transactions}
        accounts={accounts}
        earmarkings={earmarkings}
        organizationId={organizationId}
      />
    </div>
  );
}

export default withAuth(TransactionsPage)
