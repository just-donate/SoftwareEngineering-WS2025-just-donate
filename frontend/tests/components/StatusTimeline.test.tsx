import React from 'react';
import { render, screen } from '@testing-library/react';
import { StatusTimeline } from '../../src/components/tracking/StatusTimeline';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock status data
const mockStatus = [
    { status: 'Donated', date: '2023-07-20', description: 'Donation received' },
    { status: 'Processed', date: '2023-07-22', description: 'Donation by NGO' },
];

describe('StatusTimeline Component', () => {
  it('renders status items correctly', () => {
    render(
      <ThemeProvider>
        <StatusTimeline status={mockStatus} />
      </ThemeProvider>
    );
    
    // Check if the status items are rendered
    const completedItems = screen.getAllByText(/Donated/i);
    expect(completedItems.length).toBe(1); // Expect only one "Donated" status

    expect(screen.getByText(/2023-07-20/i)).toBeInTheDocument();
    expect(screen.getByText(/Donation received/i)).toBeInTheDocument();

    const pendingItems = screen.getAllByText(/Processed/i);
    expect(pendingItems.length).toBe(1); // Expect only one "Processed" status

    expect(screen.getByText(/2023-07-22/i)).toBeInTheDocument();
    expect(screen.getByText(/Donation by NGO/i)).toBeInTheDocument();
  });
}); 