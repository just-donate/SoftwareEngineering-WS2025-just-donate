'use client'

import { useState } from 'react'
import { Button } from "../../../components/organization/ui/button"
import { Input } from "../../../components/organization/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../../../components/organization/ui/select"
import { Card, CardHeader, CardTitle, CardContent } from "../../../components/organization/ui/card"
import { Transaction, BankAccount, Earmarking } from '../../../types/types'

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [amount, setAmount] = useState('')
  const [fromAccount, setFromAccount] = useState('')
  const [toAccount, setToAccount] = useState('')
  const [earmarking, setEarmarking] = useState('')
  const [transactionType, setTransactionType] = useState<'transfer' | 'withdrawal'>('transfer')

  // These should be fetched from your backend in a real application
  const accounts: BankAccount[] = [
    { id: '1', name: 'Main Account', balance: 1000, organizationId: '1', parentIds: [] },
    { id: '2', name: 'Savings Account', balance: 500, organizationId: '1', parentIds: ['1'] },
    { id: '3', name: 'Investment Account', balance: 2000, organizationId: '1', parentIds: ['1', '2'] },
  ]
  const earmarkings: Earmarking[] = [
    { id: '1', name: 'Project A', organizationId: '1' },
    { id: '2', name: 'Project B', organizationId: '1' },
  ]

  const createTransaction = () => {
    if (amount && fromAccount && (transactionType === 'transfer' ? toAccount : earmarking)) {
      const newTransaction: Transaction = {
        id: Date.now().toString(),
        amount: parseFloat(amount),
        fromAccountId: fromAccount,
        toAccountId: transactionType === 'transfer' ? toAccount : null,
        earmarkingId: transactionType === 'withdrawal' ? earmarking : null,
        type: transactionType,
      }
      setTransactions([...transactions, newTransaction])
      setAmount('')
      setFromAccount('')
      setToAccount('')
      setEarmarking('')
    }
  }

  const isValidTransfer = (from: string, to: string): boolean => {
    const fromAccount = accounts.find(a => a.id === from)
    const toAccount = accounts.find(a => a.id === to)
    if (!fromAccount || !toAccount) return false

    // Check if 'to' account is a parent of 'from' account
    return toAccount.parentIds.includes(from) || fromAccount.parentIds.includes(to)
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Create Transaction</h1>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>New Transaction</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4">
            <Select onValueChange={(value: string) => setTransactionType(value as 'transfer' | 'withdrawal')}>
              <SelectTrigger>
                <SelectValue placeholder="Transaction type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="transfer">Transfer</SelectItem>
                <SelectItem value="withdrawal">Withdrawal</SelectItem>
              </SelectContent>
            </Select>
            <Input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="Amount"
            />
            <Select onValueChange={setFromAccount}>
              <SelectTrigger>
                <SelectValue placeholder="From account" />
              </SelectTrigger>
              <SelectContent>
                {accounts.map((account) => (
                  <SelectItem key={account.id} value={account.id}>
                    {account.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {transactionType === 'transfer' ? (
              <Select 
                onValueChange={setToAccount}
                disabled={!fromAccount}
              >
                <SelectTrigger>
                  <SelectValue placeholder="To account" />
                </SelectTrigger>
                <SelectContent>
                  {accounts
                    .filter((account) => account.id !== fromAccount && isValidTransfer(fromAccount, account.id))
                    .map((account) => (
                      <SelectItem key={account.id} value={account.id}>
                        {account.name}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            ) : (
              <Select onValueChange={setEarmarking}>
                <SelectTrigger>
                  <SelectValue placeholder="Earmarking" />
                </SelectTrigger>
                <SelectContent>
                  {earmarkings.map((earmarking) => (
                    <SelectItem key={earmarking.id} value={earmarking.id}>
                      {earmarking.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
            <Button onClick={createTransaction}>Create Transaction</Button>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>Recent Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          <ul>
            {transactions.map((transaction) => (
              <li key={transaction.id}>
                {transaction.type === 'transfer'
                  ? `Transfer: $${transaction.amount} from ${accounts.find(a => a.id === transaction.fromAccountId)?.name} to ${accounts.find(a => a.id === transaction.toAccountId)?.name}`
                  : `Withdrawal: $${transaction.amount} from ${accounts.find(a => a.id === transaction.fromAccountId)?.name} (${earmarkings.find(e => e.id === transaction.earmarkingId)?.name})`}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
}

