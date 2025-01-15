import React, { act } from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { StatusTimeline } from '../../src/components/tracking/StatusTimeline';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock status data including 'allocated', 'in use', and 'used'
const mockStatus = [
  { status: 'received', date: '2023-07-20', description: 'Donation received' },
  {
    status: 'in_transfer',
    date: '2023-07-21',
    description: 'Funds sent to project',
  },
  {
    status: 'awaiting_utilization',
    date: '2023-07-22',
    description: 'Funds are awaiting utilization',
  },
  {
    status: 'used',
    date: '2023-07-23',
    description: 'Donation used by NGO',
  },
];

describe('StatusTimeline Component', () => {
  it('renders status items correctly', async () => {
    await act(async () => {
      render(
        <ThemeProvider>
          <StatusTimeline status={mockStatus} />
        </ThemeProvider>,
      );
    });

    // Check if the status items are rendered
    await waitFor(() => {
      expect(screen.getByText(/Received/)).toBeInTheDocument();
      expect(screen.getByText(/July 20, 2023/i)).toBeInTheDocument();
      expect(screen.getByText(/Donation received/i)).toBeInTheDocument();

      expect(screen.getByText(/In Transfer/)).toBeInTheDocument();
      expect(screen.getByText(/July 21, 2023/i)).toBeInTheDocument();
      expect(screen.getByText(/Funds sent to project/i)).toBeInTheDocument();

      expect(screen.getByText(/Awaiting Utilization/)).toBeInTheDocument();
      expect(screen.getByText(/July 22, 2023/i)).toBeInTheDocument();
      expect(
        screen.getByText(/Funds are awaiting utilization/i),
      ).toBeInTheDocument();

      expect(screen.getByText(/Used/)).toBeInTheDocument();
      expect(screen.getByText(/July 23, 2023/i)).toBeInTheDocument();
      expect(screen.getByText(/Donation used by NGO/i)).toBeInTheDocument();
    });
  });
});
