import React, { act } from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DonationList } from '../../src/components/tracking/DonationList';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock donation data
const mockDonations = [
  {
    donationId: '1',
    donorEmail: 'donor1@example.com',
    organisation: 'Test NGO 1',
    organisationId: '12345',
    earmarking: 'General Purpose 1',
    amount: { amount: '100.0' },
    date: '2023-01-01',
    status: [
      {
        status: 'received',
        date: '2023-01-01',
        description: 'Donation completed successfully',
      },
    ],
  },
  {
    donationId: '2',
    donorEmail: 'donor2@example.com',
    organisation: 'Test NGO 2',
    organisationId: '12345',
    earmarking: 'General Purpose 2',
    amount: { amount: '200.0' },
    date: '2023-01-02',
    status: [
      {
        status: 'in_transfer',
        date: '2023-01-02',
        description: 'Donation is pending',
      },
    ],
  },
];

describe('DonationList Component', () => {
  it('renders donation items correctly', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <DonationList donations={mockDonations} />
        </ThemeProvider>,
      );
    });

    await waitFor(async () => {
      // Check if the donation items are rendered
      expect(screen.getByText(/Test NGO 1/i)).toBeInTheDocument();
      expect(screen.getByText(/General Purpose 1/i)).toBeInTheDocument();
      expect(screen.getByText(/100.0 Euro/i)).toBeInTheDocument();
      expect(screen.getByText(/Received/i)).toBeInTheDocument();

      expect(screen.getByText(/Test NGO 2/i)).toBeInTheDocument();
      expect(screen.getByText(/General Purpose 2/i)).toBeInTheDocument();
      expect(screen.getByText(/200.0 Euro/i)).toBeInTheDocument();
      expect(screen.getByText(/In Transfer/i)).toBeInTheDocument();
    });
  });

  it('opens and closes donation details when an item is clicked', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <DonationList donations={mockDonations} />
        </ThemeProvider>,
      );
    });

    await waitFor(async () => {
      // Simulate a click on the first donation item
      fireEvent.click(screen.getByText(/Test NGO 1/i));
    });

    // Wait for the donation details to be displayed
    await waitFor(() => {
      expect(screen.getByText(/Donation Details/i)).toBeInTheDocument(); // Check if DonationDetails is rendered
    });

    // Simulate closing the donation details using the close button
    await waitFor(() => {
      fireEvent.click(screen.getByTestId('close-button')); // Use the data-testid to select the button
    });

    // Wait for the donation details to be removed
    await waitFor(() => {
      expect(screen.queryByText(/Donation Details/i)).not.toBeInTheDocument(); // Check if DonationDetails is removed
    });
  });
});
