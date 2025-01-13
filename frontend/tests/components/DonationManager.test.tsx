import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import DonationManager from '../../src/components/organization/DonationManager';
import { BankAccount, Donation, Earmarking } from '../../src/types/types';
import { createDonation, fetchDonations } from '../../src/app/organization/donations/donations';
import { fetchEarmarkings } from '../../src/app/organization/earmarkings/earmarkings';
import { fetchBankAccounts } from '../../src/app/organization/bank-accounts/bank-accounts';
import '@testing-library/jest-dom';

jest.mock('../../src/app/organization/donations/donations', () => ({
  createDonation: jest.fn(),
  fetchDonations: jest.fn(),
}));

jest.mock('../../src/app/organization/bank-accounts/bank-accounts', () => ({
  fetchBankAccounts: jest.fn(),
}));

jest.mock('../../src/app/organization/earmarkings/earmarkings', () => ({
  fetchEarmarkings: jest.fn(),
}));

const mockInitialDonations: Donation[] = [
  {
    donationId: '1',
    donorEmail: 'john@example.com',
    amount: { amount: '100.0' },
    earmarking: 'General Purpose',
    organisation: 'Organization 1',
    date: '2024-01-01',
    status: [
      {
        status: 'Pending',
        date: '2024-01-01',
        description: 'Pending',
      },
    ],
  },
];

const mockBankAccounts: BankAccount[] = [
  {
    name: 'Bank Account 1',
    balance: { amount: '100.0' },
  },
];

const mockEarmarkings: Earmarking[] = [
  {
    name: 'General Purpose',
  },
  {
    name: 'Special Purpose',
  },
];

describe('DonationManager Component', () => {
  const organizationId = '12345';

  beforeEach(() => {
    (fetchDonations as jest.Mock).mockResolvedValue(mockInitialDonations);
    (fetchBankAccounts as jest.Mock).mockResolvedValue(mockBankAccounts);
    (fetchEarmarkings as jest.Mock).mockResolvedValue(mockEarmarkings);
    render(
      <DonationManager initialDonations={mockInitialDonations} organizationId={organizationId} />
    );
  });

  it('renders initial donations', () => {
    expect(screen.getByText(/Donations/i)).toBeInTheDocument();
    expect(screen.getByText(/Organization 1/i)).toBeInTheDocument();
    expect(screen.getByText(/100.0/i)).toBeInTheDocument();
  });

  it('creates a new donation', async () => {
    (createDonation as jest.Mock).mockResolvedValueOnce({ success: true });

    fireEvent.change(screen.getByPlaceholderText(/Donor Name/i), { target: { value: 'Jane Doe' } });
    fireEvent.change(screen.getByPlaceholderText(/Donor Email/i), { target: { value: 'jane@example.com' } });
    fireEvent.change(screen.getByPlaceholderText(/Amount/i), { target: { value: '50.0' } });

    // Open the Select for earmarking
    fireEvent.click(screen.getByText(/Select Earmarking/i));
    fireEvent.click(screen.getByText(/Special Purpose/i)); // Select the option

    // Open the Select for account
    fireEvent.click(screen.getByText(/Select Account/i));
    fireEvent.click(screen.getByText(/Bank Account 1/i)); // Select the option

    fireEvent.click(screen.getByText(/Create Donation/i));

    await waitFor(() => {
      expect(screen.getByText(/Donation created successfully/i)).toBeInTheDocument();
    });
  });

  it('creates a new donation with empty fields', async () => {
    (createDonation as jest.Mock).mockResolvedValueOnce({ success: false, error: 'Error' });

    fireEvent.click(screen.getByText(/Create Donation/i));

    await waitFor(() => {
      expect(screen.getByText(/Please fill in all fields/i)).toBeInTheDocument();
    });
  });

  it('creates a new donation with error', async () => {

    (createDonation as jest.Mock).mockResolvedValueOnce({ success: false, error: 'Error' });

    fireEvent.change(screen.getByPlaceholderText(/Donor Name/i), { target: { value: 'Jane Doe' } });
    fireEvent.change(screen.getByPlaceholderText(/Donor Email/i), { target: { value: 'jane@example.com' } });
    fireEvent.change(screen.getByPlaceholderText(/Amount/i), { target: { value: '50.0' } });

    // Open the Select for earmarking
    fireEvent.click(screen.getByText(/Select Earmarking/i));
    fireEvent.click(screen.getByText(/Special Purpose/i)); // Select the option

    // Open the Select for account
    fireEvent.click(screen.getByText(/Select Account/i));
    fireEvent.click(screen.getByText(/Bank Account 1/i)); // Select the option

    fireEvent.click(screen.getByText(/Create Donation/i));

    await waitFor(() => {
      expect(screen.getByText(/Error/i)).toBeInTheDocument();
    });
  });

  it('shows an error message when fields are missing', async () => {
    fireEvent.click(screen.getByText(/Create Donation/i));

    await waitFor(() => {
      expect(screen.getByText(/Please fill in all fields/i)).toBeInTheDocument();
    });
  });
}); 