'use server'

import { revalidatePath } from 'next/cache'
import { BankAccount } from '@/types/types'

const API_URL = process.env.NEXT_PUBLIC_API_URL

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set')
}

export async function getBankAccounts(orgId: string): Promise<BankAccount[]> {
  try {
    const response = await fetch(`${API_URL}/organisation/${orgId}/account/list`, {
      cache: 'no-store'
    })
    
    if (!response.ok) {
      throw new Error('Failed to fetch bank accounts')
    }
    
    return response.json()
  } catch (error) {
    console.error('Failed to fetch bank accounts:', error)
    return []
  }
}

export async function createBankAccount(orgId: string, name: string, amount: string) {
  try {
    const response = await fetch(`${API_URL}/organisation/${orgId}/account`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ 
        name,
        balance: {
          amount
        }
      }),
    })

    if (!response.ok) {
      throw new Error('Failed to create bank account')
    }

    revalidatePath('/organization/bank-accounts')
    return { success: true }
  } catch (error) {
    return { 
      success: false, 
      error: error instanceof Error ? error.message : 'Failed to create bank account'
    }
  }
} 