import { Money, Transaction } from '@/types/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export async function fetchTransactions(orgId: string): Promise<Transaction[]> {
  try {
    const response = await fetch(
      `${API_URL}/organisation/${orgId}/transaction/list`,
    );

    if (!response.ok) {
      throw new Error('Failed to fetch bank accounts');
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Failed to fetch bank accounts:', error);
    return [];
  }
}

export async function createTransfer(
  orgId: string,
  fromAccount: string,
  toAccount: string,
  amount: {
    amount: string;
  },
) {
  try {
    const response = await fetch(`${API_URL}/transfer/${orgId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        fromAccount,
        toAccount,
        amount,
      }),
    });

    if (!response.ok) {
      throw new Error('Failed to create transfer');
    }

    return { success: true };
  } catch (error) {
    return {
      success: false,
      error:
        error instanceof Error ? error.message : 'Failed to create transfer',
    };
  }
}

export async function createWithdrawal(
  orgId: string,
  fromAccount: string,
  earmarking: string,
  amount: Money,
  description: string,
) {
  try {
    const response = await fetch(`${API_URL}/withdraw/${orgId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        fromAccount,
        earmarking,
        amount,
        description,
      }),
    });

    if (!response.ok) {
      throw new Error('Failed to create withdrawal');
    }

    return { success: true };
  } catch (error) {
    return {
      success: false,
      error:
        error instanceof Error ? error.message : 'Failed to create withdrawal',
    };
  }
}
