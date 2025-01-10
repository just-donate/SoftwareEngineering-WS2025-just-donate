import { getTransactions } from '@/app/actions/transaction'
import { getBankAccounts } from '@/app/actions/bank-account'
import { getEarmarkings } from '@/app/actions/earmarking'
import TransactionManager from '@/components/organization/TransactionManager'

export default async function TransactionsPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920'
  
  // Fetch all required data in parallel
  const [transactions, accounts, earmarkings] = await Promise.all([
    getTransactions(organizationId),
    getBankAccounts(organizationId),
    getEarmarkings(organizationId)
  ])

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
  )
}

