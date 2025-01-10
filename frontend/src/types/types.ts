export interface DonationStatus {
  status: string;
  date: string;
  description: string;
}

export interface Money {
  amount: string;
}

export interface Donation {
  donationId: string;
  donorEmail: string;
  amount: Money;
  organisation: string;
  date: string;
  earmarking: string;
  status: DonationStatus[];
}

export interface Donations {
  donations: Donation[];
}

export interface Organization {
  id: string;
  name: string;
}

export interface Earmarking {
  name: string;
}

export interface Transaction {
  amount: number;
  fromAccountId: string;
  toAccountId: string | null;
  earmarkingId: string | null;
  type: 'transfer' | 'withdrawal';
}

export interface Photo {
  id: string;
  url: string;
  earmarkingId: string;
  uploadDate: string;
}

export interface BankAccount {
  name: string;
  balance: Money;
}
