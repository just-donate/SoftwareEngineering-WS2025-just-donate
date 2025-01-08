import { Donation, Donations } from "@/types/types";

export async function getDonations(id: string): Promise<Donations | null> {

  try {
    const response = await fetch(`${process.env.API_URL}/donate/591671920/donor/${id}`);
    if (!response.ok) return null;
    return response.json();
  } catch (_) {
    return null;
  }
}

