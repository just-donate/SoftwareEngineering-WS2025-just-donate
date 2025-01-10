'use client'

import { useState } from 'react'
import { Button } from "./ui/button"
import { Input } from "./ui/input"
import { Card, CardHeader, CardTitle, CardContent } from "./ui/card"
import { BankAccount } from '@/types/types'
import { createBankAccount } from '@/app/actions/bank-account'

interface BankAccountManagerProps {
  initialAccounts: BankAccount[]
  organizationId: string
}

export default function BankAccountManager({ initialAccounts, organizationId }: BankAccountManagerProps) {
  const [accounts, setAccounts] = useState<BankAccount[]>(initialAccounts)
  const [newAccountName, setNewAccountName] = useState('')
  const [newAccountAmount, setNewAccountAmount] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const addBankAccount = async () => {
    if (!newAccountName || !newAccountAmount) return

    const result = await createBankAccount(organizationId, newAccountName, newAccountAmount)
    
    if (result.success) {
      // Optimistically update the UI
      const newAccount: BankAccount = {
        name: newAccountName,
        balance: {
          amount: newAccountAmount
        }
      }
      setAccounts([...accounts, newAccount])
      setNewAccountName('')
      setNewAccountAmount('')
      setSuccessMessage('Bank account created successfully!')
      setError('')
      setTimeout(() => setSuccessMessage(''), 3000)
    } else {
      setError(result.error || 'Failed to create bank account')
      setSuccessMessage('')
    }
  }

  return (
    <div>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Add New Bank Account</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col space-y-2">
              <Input
                value={newAccountName}
                onChange={(e) => setNewAccountName(e.target.value)}
                placeholder="Account name"
                className="mb-2"
              />
              <Input
                value={newAccountAmount}
                onChange={(e) => setNewAccountAmount(e.target.value)}
                placeholder="Initial balance"
                type="number"
                step="0.01"
              />
              <Button onClick={addBankAccount} className="mt-2">Add Account</Button>
            </div>
            {error && <div className="text-red-500">{error}</div>}
            {successMessage && <div className="text-green-500">{successMessage}</div>}
          </div>
        </CardContent>
      </Card>
      
      <Card>
        <CardHeader>
          <CardTitle>Existing Bank Accounts</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {accounts.map((account) => (
              <li key={account.name} className="p-4 bg-secondary rounded-lg flex justify-between items-center">
                <span>{account.name}</span>
                <span className="font-medium">{account.balance.amount}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
} 