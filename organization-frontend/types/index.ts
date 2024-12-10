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

export interface Donation {
  id: string;
  donorEmail: string;
  amount: number;
  status: 'received' | 'transferred' | 'in use' | 'used';
  lastUpdated: string;
}

export interface Photo {
  id: string;
  url: string;
  earmarkingId: string;
  uploadDate: string;
}

