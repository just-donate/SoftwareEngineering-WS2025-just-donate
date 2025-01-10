'use client'

import { useState } from 'react'
import { Button } from "./ui/button"
import { Input } from "./ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select"
import { Card, CardHeader, CardTitle, CardContent } from "./ui/card"
import { Transaction, BankAccount, Earmarking } from '@/types/types'
import { createTransfer, createWithdrawal } from '@/app/actions/transaction'

interface TransactionManagerProps {
  initialTransactions: Transaction[]
  accounts: BankAccount[]
  earmarkings: Earmarking[]
  organizationId: string
}

export default function TransactionManager({ 
  initialTransactions,
  accounts,
  earmarkings,
  organizationId
}: TransactionManagerProps) {
  const [transactions, setTransactions] = useState<Transaction[]>(initialTransactions)
  const [amount, setAmount] = useState('')
  const [fromAccount, setFromAccount] = useState('')
  const [toAccount, setToAccount] = useState('')
  const [earmarking, setEarmarking] = useState('')
  const [transactionType, setTransactionType] = useState<'transfer' | 'withdrawal'>('transfer')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const createTransaction = async () => {
    if (!amount || !fromAccount || (transactionType === 'transfer' ? !toAccount : !earmarking)) {
      setError('Please fill in all required fields')
      return
    }

    try {
      const result = transactionType === 'transfer'
        ? await createTransfer(organizationId, fromAccount, toAccount, amount)
        : await createWithdrawal(organizationId, fromAccount, earmarking, amount, Date.now().toString())

      if (result.success) {
        // Optimistically update the UI
        const newTransaction: Transaction = {
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
        setSuccessMessage('Transaction created successfully!')
        setError('')
        setTimeout(() => setSuccessMessage(''), 3000)
      } else {
        setError(result.error || 'Failed to create transaction')
      }
    } catch (error) {
      setError('An error occurred while creating the transaction')
    }
  }

  const isValidTransfer = (from: string, to: string): boolean => {
    const fromAcc = accounts.find(a => a.name === from)
    const toAcc = accounts.find(a => a.name === to)
    if (!fromAcc || !toAcc) return false
    return true // Add your transfer validation logic here
  }

  return (
    <div>
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
              step="0.01"
            />

            <Select onValueChange={setFromAccount}>
              <SelectTrigger>
                <SelectValue placeholder="From account" />
              </SelectTrigger>
              <SelectContent>
                {accounts.map((account) => (
                  <SelectItem key={account.name} value={account.name}>
                    {account.name} ({account.balance.amount})
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
                    .filter(account => account.name !== fromAccount && isValidTransfer(fromAccount, account.name))
                    .map(account => (
                      <SelectItem key={account.name} value={account.name}>
                        {account.name} ({account.balance.amount})
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
                    <SelectItem key={earmarking.name} value={earmarking.name}>
                      {earmarking.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}

            {error && <div className="text-red-500">{error}</div>}
            {successMessage && <div className="text-green-500">{successMessage}</div>}
            
            <Button onClick={createTransaction}>Create Transaction</Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Recent Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {transactions.map((transaction, index) => (
              <li key={index} className="p-4 bg-secondary rounded-lg">
                {transaction.type === 'transfer'
                  ? `Transfer: ${transaction.amount} from ${accounts.find(a => a.name === transaction.fromAccountId)?.name} to ${accounts.find(a => a.name === transaction.toAccountId)?.name}`
                  : `Withdrawal: ${transaction.amount} from ${accounts.find(a => a.name === transaction.fromAccountId)?.name} (${earmarkings.find(e => e.name === transaction.earmarkingId)?.name})`}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
} 