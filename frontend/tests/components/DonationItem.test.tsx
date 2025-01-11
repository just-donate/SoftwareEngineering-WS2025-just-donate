import React from 'react';
import { render, screen } from '@testing-library/react';
import { DonationItem } from '../../src/components/tracking/DonationItem';
import { ThemeProvider } from '../../src/contexts/ThemeContext';
import '@testing-library/jest-dom'; // Import jest-dom

// Mock donation data
const mockDonation = {
  id: '1',
  donorEmail: 'donor@example.com',
  ngo: 'Test NGO',
  project: 'Test Project',
  amount: 100,
  currency: 'USD',
  date: '2023-01-01',
  status: [{ status: 'Completed', date: '2023-01-02', description: 'Donation completed successfully' }],
};

const mockOnClick = jest.fn();

describe('DonationItem Component', () => {
  it('renders donation details correctly', () => {
    render(
      <ThemeProvider>
        <DonationItem donation={mockDonation} onClick={mockOnClick} />
      </ThemeProvider>
    );

    // Check if the NGO name is rendered
    expect(screen.getByText(/Test NGO/i)).toBeInTheDocument();
    
    // Check if the project name is rendered
    expect(screen.getByText(/Test Project/i)).toBeInTheDocument();
    
    // Check if the amount and currency are rendered
    expect(screen.getByText(/100 USD/i)).toBeInTheDocument();
    
    // Check if the donation date is rendered
    expect(screen.getByText(/2023-01-01/i)).toBeInTheDocument();
    
    // Check if the status is rendered
    expect(screen.getByText(/Completed/i)).toBeInTheDocument();
  });

  it('calls onClick when the item is clicked', () => {
    render(
      <ThemeProvider>
        <DonationItem donation={mockDonation} onClick={mockOnClick} />
      </ThemeProvider>
    );

    // Simulate a click on the donation item
    screen.getByText(/Test NGO/i).click();

    // Check if the onClick function was called
    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });
}); 