import { getBankAccounts } from '@/app/actions/bank-account';
import { getEarmarkings } from '@/app/actions/earmarking';
import WithdrawalManager from '@/components/organization/WithdrawalManager';

export default async function WithdrawalPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  const [accounts, earmarkings] = await Promise.all([
    getBankAccounts(organizationId),
    getEarmarkings(organizationId),
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
