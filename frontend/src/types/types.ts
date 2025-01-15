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
  amount: Money;
  organisation: string;
  date: string;
  earmarking: string;
  status: DonationStatus[];
}

export interface Donor {
  id: string;
  name: string;
  email: string;
}

export interface DonationWithDonor {
  donationId: string;
  donor: Donor;
  amount: Money;
  organisation: string;
  date: string;
  earmarking?: string;
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
  description: string;
}

export interface EarmarkingImage {
  image: {
    fileUrl: string;
  };
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
  byEarmarking: Array<[string, Money]>;
}

export interface Withdrawal {
  id: string;
  amount: Money;
  time: string;
  fromAccount: string;
  description: string;
  earmarking?: string;
}

export interface WithdrawalList {
  expenses: Withdrawal[];
}
