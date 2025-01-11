import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DonationList } from '../../src/components/tracking/DonationList';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock donation data
const mockDonations = [
  {
    id: '1',
    donorEmail: 'donor1@example.com',
    ngo: 'Test NGO 1',
    project: 'Test Project 1',
    amount: 100,
    currency: 'USD',
    date: '2023-01-01',
    status: [{ status: 'Completed', date: '2023-01-01', description: 'Donation completed successfully' }],
  },
  {
    id: '2',
    donorEmail: 'donor2@example.com',
    ngo: 'Test NGO 2',
    project: 'Test Project 2',
    amount: 200,
    currency: 'USD',
    date: '2023-01-02',
    status: [{ status: 'Pending', date: '2023-01-02', description: 'Donation is pending' }],
  },
];

describe('DonationList Component', () => {
  it('renders donation items correctly', () => {
    render(
      <ThemeProvider>
        <DonationList donations={mockDonations} />
      </ThemeProvider>
    );

    // Check if the donation items are rendered
    expect(screen.getByText(/Test NGO 1/i)).toBeInTheDocument();
    expect(screen.getByText(/Test Project 1/i)).toBeInTheDocument();
    expect(screen.getByText(/100 USD/i)).toBeInTheDocument();
    expect(screen.getByText(/Completed/i)).toBeInTheDocument();

    expect(screen.getByText(/Test NGO 2/i)).toBeInTheDocument();
    expect(screen.getByText(/Test Project 2/i)).toBeInTheDocument();
    expect(screen.getByText(/200 USD/i)).toBeInTheDocument();
    expect(screen.getByText(/Pending/i)).toBeInTheDocument();
  });

  it('opens donation details when an item is clicked', async () => {
    render(
      <ThemeProvider>
        <DonationList donations={mockDonations} />
      </ThemeProvider>
    );

    // Simulate a click on the first donation item
    fireEvent.click(screen.getByText(/Test NGO 1/i));

    // Wait for the donation details to be displayed
    await waitFor(() => {
      expect(screen.getByText(/Donation Details/i)).toBeInTheDocument();
    });
  });
}); 