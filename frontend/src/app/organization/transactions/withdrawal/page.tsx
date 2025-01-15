'use client';

import WithdrawalManager from '@/components/organization/WithdrawalManager';
import { WithdrawalList } from '@/components/organization/WithdrawalList';
import { fetchEarmarkings } from '../../earmarkings/earmarkings';
import { fetchBankAccounts } from '../../bank-accounts/bank-accounts';
import { fetchWithdrawals } from '../transactions';
import { useEffect, useState } from 'react';
import { BankAccount, Earmarking, Withdrawal } from '@/types/types';
import withAuth from '../../api/RequiresAuth';

function WithdrawalPage() {
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([]);
  const [withdrawals, setWithdrawals] = useState<Withdrawal[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [accountsData, earmarkingsData, withdrawalsData] = await Promise.all([
          fetchBankAccounts(organizationId),
          fetchEarmarkings(organizationId),
          fetchWithdrawals(organizationId),
        ]);

        setAccounts(accountsData);
        setEarmarkings(earmarkingsData);
        setWithdrawals(withdrawalsData.expenses);
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
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold mb-4">Create Withdrawal</h1>
        <WithdrawalManager
          accounts={accounts}
          earmarkings={earmarkings}
          organizationId={organizationId}
        />
      </div>
      
      <div>
        <WithdrawalList withdrawals={withdrawals} />
      </div>
    </div>
  );
}

export default withAuth(WithdrawalPage);
