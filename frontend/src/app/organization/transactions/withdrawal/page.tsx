import WithdrawalManager from '@/components/organization/WithdrawalManager';
import { fetchEarmarkings } from '../../earmarkings/earmarkings';
import { fetchBankAccounts } from '../../bank-accounts/bank-accounts';

export default async function WithdrawalPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  const [accounts, earmarkings] = await Promise.all([
    fetchBankAccounts(organizationId),
    fetchEarmarkings(organizationId),
  ]);

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
