import { getBankAccounts } from '@/app/actions/bank-account'
import TransferManager from '@/components/organization/TransferManager'

export default async function TransferPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920'
  
  const accounts = await getBankAccounts(organizationId)

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Create Transfer</h1>
      <TransferManager 
        accounts={accounts}
        organizationId={organizationId}
      />
    </div>
  )
} 