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
  donorEmail: string;
}

export interface Organization {
  id: string;
  name: string;
}

export interface Earmarking {
  id: string;
  name: string;
  organizationId: string;
}

export interface BankAccount {
  id: string;
  name: string;
  balance: number;
  organizationId: string;
  parentIds: string[];
}

export interface Transaction {
  id: string;
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
  id: string
  name: string
  balance: number
  parentIds: string[]
} 
