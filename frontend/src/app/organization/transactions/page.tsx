import TransactionManager from '@/components/organization/TransactionManager';
import { fetchEarmarkings } from '../earmarkings/earmarkings';
import { fetchBankAccounts } from '../bank-accounts/bank-accounts';
import { fetchTransactions } from './transactions';

export default async function TransactionsPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  // Fetch all required data in parallel
  const [transactions, accounts, earmarkings] = await Promise.all([
    fetchTransactions(organizationId),
    fetchBankAccounts(organizationId),
    fetchEarmarkings(organizationId),
  ]);

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
