import { getBankAccounts } from '@/app/actions/bank-account'
import BankAccountManager from '@/components/organization/BankAccountManager'

export default async function BankAccountsPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920'
  const accounts = await getBankAccounts(organizationId)

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Bank Accounts</h1>
      <BankAccountManager 
        initialAccounts={accounts}
        organizationId={organizationId}
      />
    </div>
  )
}

