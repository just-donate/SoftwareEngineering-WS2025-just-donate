'use client';

import { useState, useEffect } from 'react';
import BankAccountManager from '@/components/organization/BankAccountManager';
import { BankAccount } from '@/types/types';
import { fetchBankAccounts } from './bank-accounts';
import withAuth from '../api/RequiresAuth';
import { config } from '@/lib/config';

function BankAccountsPage() {
  const [accounts, setAccounts] = useState<BankAccount[]>([]);
  // TODO: Get the organization ID from the session/context
  const organizationId = config.organizationId;

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

export default withAuth(BankAccountsPage);
