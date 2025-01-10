import { Donation, Donations } from "@/types/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set')
}

export async function getDonations(id: string): Promise<Donations | null> {

  try {
    const response = await fetch(`${API_URL}/donate/591671920/donor/${id}`);
    if (!response.ok) return null;
    return response.json();
  } catch (_) {
    return null;
  }
}

