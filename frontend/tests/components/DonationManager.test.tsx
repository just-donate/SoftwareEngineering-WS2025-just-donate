import React, { act } from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import DonationManager from '../../src/components/organization/DonationManager';
import { BankAccount, Donation, Earmarking } from '../../src/types/types';
import {
  createDonation,
  fetchDonations,
} from '../../src/app/organization/donations/donations';
import { fetchEarmarkings } from '../../src/app/organization/earmarkings/earmarkings';
import { fetchBankAccounts } from '../../src/app/organization/bank-accounts/bank-accounts';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
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
    byEarmarking: [
      ['General Purpose', { amount: '500.0' }],
      ['Special Purpose', { amount: '500.0' }],
    ],
  },
];

const mockEarmarkings: Earmarking[] = [
  {
    name: 'General Purpose',
    description: 'General purpose earmarking',
  },
  {
    name: 'Special Purpose',
    description: 'Special purpose earmarking',
  },
];

// Mock the theme provide

describe('DonationManager Component', () => {
  const organizationId = '12345';

  beforeEach(() => {
    (fetchDonations as jest.Mock).mockResolvedValue(mockInitialDonations);
    (fetchBankAccounts as jest.Mock).mockResolvedValue(mockBankAccounts);
    (fetchEarmarkings as jest.Mock).mockResolvedValue(mockEarmarkings);
  });

  const renderWithThemeProvider = (component: React.ReactNode) => {
    const mockTheme = {
      primary: '#ffffff',
      secondary: '#000000',
      accent: '#ff0000',
      background: '#f0f0f0',
      card: '#ffffff',
      text: '#000000',
      textLight: '#ffffff',
      font: 'Arial',
      icon: 'icon-url',
      ngoName: 'Mock NGO',
      ngoUrl: 'http://mockngo.com',
      helpUrl: 'http://mockngo.com/help',
      statusColors: {
        announced: '#ffcc00',
        pending_confirmation: '#ff9900',
        confirmed: '#00cc00',
        received: '#0000cc',
        in_transfer: '#cc00cc',
        processing: '#cccc00',
        allocated: '#00cccc',
        awaiting_utilization: '#cc0000',
        used: '#cccccc',
      },
    };

    const updateTheme = jest.fn();

    return render(
      <ThemeProvider>
        {component}
      </ThemeProvider>
    );
  };

  it('renders initial donations', async () => {
    await act(async () => {
      renderWithThemeProvider(
        <DonationManager
          initialDonations={mockInitialDonations}
          organizationId={organizationId}
        />,
      );
    });

    await waitFor(() => {
      expect(screen.getByText(/Donations/i)).toBeInTheDocument();
      expect(screen.getByText(/Organization 1/i)).toBeInTheDocument();
      expect(screen.getByText(/100.0/i)).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('creates a new donation', async () => {
    (createDonation as jest.Mock).mockResolvedValueOnce({ success: true });

    await act(async () => {
      renderWithThemeProvider(
        <DonationManager
          initialDonations={mockInitialDonations}
          organizationId={organizationId}
        />,
      );
    });

    await act(async () => {
      fireEvent.change(screen.getByTestId('donor-name-input'), {
        target: { value: 'Jane Doe' },
      });
      fireEvent.change(screen.getByTestId('donor-email-input'), {
        target: { value: 'jane@example.com' },
      });
      fireEvent.change(screen.getByTestId('amount-input'), {
        target: { value: '50.0' },
      });

      // Open the Select for earmarking
      fireEvent.click(screen.getByText(/Select Earmarking/i));
      fireEvent.click(screen.getByText(/Special Purpose/i)); // Select the option

      // Open the Select for account
      fireEvent.click(screen.getByText(/Select Account/i));
      fireEvent.click(screen.getByText(/Bank Account 1/i)); // Select the option

      fireEvent.click(screen.getByText(/Create Donation/i));
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Donation created successfully/i),
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Donation created successfully/i),
      ).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('creates a new donation with empty fields', async () => {
    (createDonation as jest.Mock).mockResolvedValueOnce({
      success: false,
      error: 'Error',
    });

    await act(async () => {
      renderWithThemeProvider(
        <DonationManager
          initialDonations={mockInitialDonations}
          organizationId={organizationId}
        />,
      );
    });

    await waitFor(() => {
      fireEvent.click(screen.getByText(/Create Donation/i));
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Please fill in all fields/i),
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Please fill in all fields/i),
      ).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('creates a new donation with error', async () => {
    (createDonation as jest.Mock).mockResolvedValueOnce({
      success: false,
      error: 'Error',
    });

    await act(async () => {
      renderWithThemeProvider(
        <DonationManager
          initialDonations={mockInitialDonations}
          organizationId={organizationId}
        />,
      );
    });

    await act(async () => {
      fireEvent.change(screen.getByTestId('donor-name-input'), {
        target: { value: 'Jane Doe' },
      });
      fireEvent.change(screen.getByTestId('donor-email-input'), {
        target: { value: 'jane@example.com' },
      });
      fireEvent.change(screen.getByTestId('amount-input'), {
        target: { value: '50.0' },
      });

      // Open the Select for earmarking
      fireEvent.click(screen.getByText(/Select Earmarking/i));
      fireEvent.click(screen.getByText(/Special Purpose/i)); // Select the option

      // Open the Select for account
      fireEvent.click(screen.getByText(/Select Account/i));
      fireEvent.click(screen.getByText(/Bank Account 1/i)); // Select the option
      fireEvent.click(screen.getByText(/Create Donation/i));
    });

    await waitFor(() => {
      expect(screen.getByText(/Error/i)).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('shows an error message when fields are missing', async () => {
    await act(async () => {
      renderWithThemeProvider(
        <DonationManager
          initialDonations={mockInitialDonations}
          organizationId={organizationId}
        />,
      );
    });

    await waitFor(() => {
      fireEvent.click(screen.getByText(/Create Donation/i));
    });

    await waitFor(() => {
      expect(
        screen.getByText(/Please fill in all fields/i),
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Please fill in all fields/i),
      ).toBeInTheDocument();
    }, { timeout: 10000 });
  });
});
