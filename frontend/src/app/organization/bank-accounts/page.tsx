'use client';

import { useState, useEffect } from 'react';
import BankAccountManager from '@/components/organization/BankAccountManager';
import { BankAccount } from '@/types/types';
import { fetchBankAccounts } from './bank-accounts';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export default function BankAccountsPage() {
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  useEffect(() => {
    fetchBankAccounts(organizationId).then(setAccounts);
  }, [organizationId]);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Bank Accounts</h1>
      <BankAccountManager
        initialAccounts={accounts}
        organizationId={organizationId}
      />
    </div>
  );
}
