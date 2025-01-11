'use client';

import WithdrawalManager from '@/components/organization/WithdrawalManager';
import { fetchEarmarkings } from '../../earmarkings/earmarkings';
import { fetchBankAccounts } from '../../bank-accounts/bank-accounts';
import { useEffect, useState } from 'react';
import { BankAccount, Earmarking } from '@/types/types';

export default function WithdrawalPage() {
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [accountsData, earmarkingsData] = await Promise.all([
          fetchBankAccounts(organizationId),
          fetchEarmarkings(organizationId),
        ]);

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
      <h1 className="text-2xl font-bold mb-4">Create Withdrawal</h1>
      <WithdrawalManager
        accounts={accounts}
        earmarkings={earmarkings}
        organizationId={organizationId}
      />
    </div>
  );
}