'use server';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export interface StatusUpdate {
  status: string;
  date: string;
  description: string;
}

export interface Donation {
  donationId: string;
  amount: {
    amount: string;
  };
  organisation: string;
  date: string;
  earmarking: string;
  status: StatusUpdate[];
}

export interface DonationListResponse {
  donations: Donation[];
}

export async function getDonations(orgId: string): Promise<Donation[]> {
  try {
    const response = await fetch(`${API_URL}/donate/${orgId}/donations`, {
      cache: 'no-store',
    });

    if (!response.ok) {
      console.error(
        'Failed to fetch donations:',
        response.status,
        response.statusText,
      );
      throw new Error('Failed to fetch donations');
    }

    const data: DonationListResponse = await response.json();
    return data.donations;
  } catch (error) {
    console.error('Failed to fetch donations:', error);
    return [];
  }
}
