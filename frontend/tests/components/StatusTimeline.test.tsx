import React from 'react';
import { render, screen } from '@testing-library/react';
import { StatusTimeline } from '../../src/components/tracking/StatusTimeline';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock status data including 'allocated', 'in use', and 'used'
const mockStatus = [
  { status: 'Donated', date: '2023-07-20', description: 'Donation received' },
  {
    status: 'Allocated',
    date: '2023-07-21',
    description: 'Funds sent to project',
  },
  { status: 'In Use', date: '2023-07-22', description: 'Funds are inuse' },
  {
    status: 'Processed',
    date: '2023-07-23',
    description: 'Donation used by NGO',
  },
];



describe('StatusTimeline Component', () => {
  it('renders status items correctly', () => {
    render(
      <ThemeProvider>
        <StatusTimeline status={mockStatus} />
      </ThemeProvider>,
    );

    // Check if the status items are rendered
    expect(screen.getByText(/Donated/i)).toBeInTheDocument();
    expect(screen.getByText(/July 20, 2023 at 02:00 AM/i)).toBeInTheDocument();
    expect(screen.getByText(/Donation received/i)).toBeInTheDocument();

    expect(screen.getByText(/Allocated/i)).toBeInTheDocument();
    expect(screen.getByText(/July 21, 2023 at 02:00 AM/i)).toBeInTheDocument();
    expect(screen.getByText(/Funds sent to project/i)).toBeInTheDocument();

    expect(screen.getByText(/In Use/i)).toBeInTheDocument();
    expect(screen.getByText(/July 22, 2023 at 02:00 AM/i)).toBeInTheDocument();
    expect(screen.getByText(/Funds are inuse/i)).toBeInTheDocument();

    expect(screen.getByText(/Processed/i)).toBeInTheDocument();
    expect(screen.getByText(/July 23, 2023 at 02:00 AM/i)).toBeInTheDocument();
    expect(screen.getByText(/Donation used by NGO/i)).toBeInTheDocument();
  });
});
