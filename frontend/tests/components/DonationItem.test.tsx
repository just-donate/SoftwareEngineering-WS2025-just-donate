import React, { act } from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { DonationItem } from '../../src/components/tracking/DonationItem';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import { Donation } from '../../src/types/types';
import Cookies from 'js-cookie'; // Import js-cookie
import '@testing-library/jest-dom'; // Import jest-dom

// Mock donation data
// Mock donation data
const mockDonation = {
  donationId: '1', // Add this line
  organisation: 'Test Organisation', // Add this line
  earmarking: 'General Purpose', // Add this line
  donorEmail: 'donor@example.com',
  ngo: 'Test NGO',
  project: 'Test Project',
  amount: { amount: '100.0' },
  date: '2023-01-01',
  status: [
    {
      status: 'Completed',
      date: '2023-01-02',
      description: 'Donation completed successfully',
    },
  ],
} as Donation;

const mockOnClick = jest.fn();

describe('DonationItem Component', () => {
  beforeEach(() => {
    // Clear cookies before each test
    Cookies.remove('shownDonations');
  });

  it('renders donation details correctly', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <DonationItem donation={mockDonation} onClick={mockOnClick} />
        </ThemeProvider>,
      );
    });

    // Check if the NGO name is rendered
    expect(screen.getByText(/Test Organisation/i)).toBeInTheDocument();

    // Check if the project name is rendered
    expect(screen.getByText(/General Purpose/i)).toBeInTheDocument();

    // Check if the amount and currency are rendered
    expect(screen.getByText(/100.0 Euro/i)).toBeInTheDocument();

    // Check if the donation date is rendered
    expect(screen.getByText(/January 1, 2023/i)).toBeInTheDocument();

    // Check if the status is rendered
    expect(screen.getByText(/Completed/i)).toBeInTheDocument();
  });

  it('shows thank you message when the item is rendered', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <DonationItem donation={mockDonation} onClick={mockOnClick} />
        </ThemeProvider>,
      );
    });

    // Check if the thank you message is displayed
    expect(
      screen.getByText(/Thank you for your donation!/i),
    ).toBeInTheDocument();

    // Wait for the thank you message to exit
    await waitFor(
      () => {
        expect(
          screen.queryByText(/Thank you for your donation!/i),
        ).not.toBeInTheDocument();
      },
      { timeout: 4000 },
    ); // Increase timeout if necessary
  });

  it('calls onClick when the item is clicked', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <DonationItem donation={mockDonation} onClick={mockOnClick} />
        </ThemeProvider>,
      );
    });

    // Simulate a click on the donation item
    screen.getByText(/Test Organisation/i).click();

    // Check if the onClick function was called
    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });
});
