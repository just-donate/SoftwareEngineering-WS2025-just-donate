import React from 'react';
import { render, screen, act, waitFor } from '@testing-library/react';
import { DonationDetails } from '../../src/components/tracking/DonationDetails';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock the StatusTimeline component
jest.mock('../../src/components/tracking/StatusTimeline', () => {
  return {
    StatusTimeline: jest.fn(() => (
      <div data-testid="mock-status-timeline">Status Timeline</div>
    )),
  };
});

describe('DonationDetails Component', () => {
  const mockDonation = {
    donationId: '1',
    organisation: 'Test Organisation',
    earmarking: 'General Purpose',
    donorEmail: 'donor@example.com',
    project: 'Test Project',
    amount: { amount: '100.0' },
    date: '2023-01-01',
    status: [
      {
        status: 'received',
        date: '2023-01-01',
        description: 'Donation completed successfully',
      },
      {
        status: 'Pending',
        date: '2023-01-02',
        description: 'Donation is pending',
      },
    ],
  };

  const mockOnClose = jest.fn();

  it('renders DonationDetails correctly', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <DonationDetails donation={mockDonation} onClose={mockOnClose} />
        </ThemeProvider>,
      );
    });

    // Check if the DonationDetails component is displayed
    await waitFor(() => {
      const element = screen.getByText(/Donation Details/i);
      expect(element).toBeInTheDocument();
    });

    // Check if the StatusTimeline is rendered
    await waitFor(() => {
      const statusTimeline = screen.getByTestId('mock-status-timeline');
      expect(statusTimeline).toBeInTheDocument();
    });
  });
});
