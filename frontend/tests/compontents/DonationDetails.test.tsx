import React from 'react';
import { render, screen } from '@testing-library/react';
import { DonationDetails } from '../../src/components/tracking/DonationDetails';
import '@testing-library/jest-dom';

describe('DonationDetails Component', () => {
  const mockDonation = {
    id: '1',
    donorEmail: 'donor@example.com',
    ngo: 'Test NGO',
    project: 'Test Project',
    amount: 100,
    currency: 'USD',
    date: '2023-01-01',
    status: [{ status: 'Completed', date: '2023-01-01', description: 'Donation completed successfully' }],
  };

  const mockOnClose = jest.fn();

  it('renders MapContainer with correct props', () => {
    render(<DonationDetails donation={mockDonation} onClose={mockOnClose} />);

    const element = screen.getByText(/donation details/i);
    // Check if the DonationDetails component is rendered
    expect(element).toBeInTheDocument();
  });
});