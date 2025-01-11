import { BankAccount } from '@/types/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export async function fetchBankAccounts(orgId: string): Promise<BankAccount[]> {
  try {
    const response = await fetch(
      `${API_URL}/organisation/${orgId}/account/list`,
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
