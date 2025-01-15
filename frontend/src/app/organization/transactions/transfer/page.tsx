'use client';

import TransferManager from '@/components/organization/TransferManager';
import { fetchBankAccounts } from '../../bank-accounts/bank-accounts';
import { useEffect, useState } from 'react';
import { BankAccount } from '@/types/types';
import withAuth from '../../api/RequiresAuth';
import { config } from '@/lib/config';

function TransferPage() {
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // TODO: Get the organization ID from the session/context
  const organizationId = config.organizationId;

  const refreshAccounts = async () => {
    try {
      const accountsData = await fetchBankAccounts(organizationId);
      setAccounts(accountsData);
    } catch (error) {
      console.error('Error fetching accounts:', error);
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const accountsData = await fetchBankAccounts(organizationId);
        setAccounts(accountsData);
      } catch (error) {
        console.error('Error fetching accounts:', error);
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
      <h1 className="text-2xl font-bold mb-4">Create Transfer</h1>
      <TransferManager
        accounts={accounts}
        organizationId={organizationId}
        onTransferSuccess={refreshAccounts}
      />
    </div>
  );
}

export default withAuth(TransferPage);
