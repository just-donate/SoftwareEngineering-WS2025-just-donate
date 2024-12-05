export interface DonationStatus {
  status: string;
  date: string;
  description: string;
}

export interface Donation {
  id: string;
  amount: number;
  currency: string;
  ngo: string;
  date: string;
  project: string;
  status: DonationStatus[];
}

