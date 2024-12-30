'use client'

import { useState } from 'react'
import { Button } from "../../../components/organization/ui/button"
import { Input } from "../../../components/organization/ui/input"
import { Card, CardHeader, CardTitle, CardContent } from "../../../components/organization/ui/card"
import { Checkbox } from "@/components/organization/ui/checkbox"
import { BankAccount } from '../../../types/types'
import { Alert, AlertDescription, AlertTitle } from "@/components/organization/ui/alert"
import { AlertCircle } from 'lucide-react'
import { TreeView } from '../../../components/organization/tree-view'

export default function BankAccountsPage() {
  const [accounts, setAccounts] = useState<BankAccount[]>([])
  const [newAccountName, setNewAccountName] = useState('')
  const [newAccountBalance, setNewAccountBalance] = useState('')
  const [newAccountParents, setNewAccountParents] = useState<string[]>([])

  const addAccount = () => {
    if (newAccountName && newAccountBalance) {
      const newAccount: BankAccount = {
        id: Date.now().toString(),
        name: newAccountName,
        balance: parseFloat(newAccountBalance),
        organizationId: '1', // This should be the actual organization ID
        parentIds: newAccountParents,
      }
      setAccounts([...accounts, newAccount])
      setNewAccountName('')
      setNewAccountBalance('')
      setNewAccountParents([])
    }
  }

  const toggleParent = (accountId: string) => {
    setNewAccountParents(prev => 
      prev.includes(accountId)
        ? prev.filter(id => id !== accountId)
        : [...prev, accountId]
    )
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Bank Accounts</h1>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Add New Bank Account</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <Input
              value={newAccountName}
              onChange={(e) => setNewAccountName(e.target.value)}
              placeholder="Account name"
            />
            <Input
              type="number"
              value={newAccountBalance}
              onChange={(e) => setNewAccountBalance(e.target.value)}
              placeholder="Initial balance"
            />
            {accounts.length > 0 && (
              <div>
                <h3 className="text-lg font-semibold mb-2">Select Parent Accounts:</h3>
                {accounts.map((account) => (
                  <div key={account.id} className="flex items-center space-x-2">
                    <Checkbox
                      id={account.id}
                      checked={newAccountParents.includes(account.id)}
                      onCheckedChange={() => toggleParent(account.id)}
                    />
                    <label htmlFor={account.id}>{account.name}</label>
                  </div>
                ))}
              </div>
            )}
            <Button onClick={addAccount}>Add Account</Button>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>Bank Account Hierarchy</CardTitle>
        </CardHeader>
        <CardContent>
          {accounts.length === 0 ? (
            <Alert variant="default">
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>No bank accounts</AlertTitle>
              <AlertDescription>
                There are currently no bank accounts. Add a new bank account to get started.
              </AlertDescription>
            </Alert>
          ) : (
            <TreeView accounts={accounts} />
          )}
        </CardContent>
      </Card>
    </div>
  )
}

