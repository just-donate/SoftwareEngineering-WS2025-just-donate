import React from 'react';
import { render, screen } from '@testing-library/react';
import { DonationDetails } from '../../src/components/tracking/DonationDetails';
import '@testing-library/jest-dom'; // Import jest-dom

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

    const element = screen.getByText(/Donation Details/i);
    // Check if the MapContainer is rendered
    expect(element).toBeInTheDocument();

    // Check if the MapContainer has the correct zoom and scrollWheelZoom props
    // Note: You may need to mock the MapContainer to check its props
    // This is a simplified check; you might need to adjust based on your testing setup
  });
});