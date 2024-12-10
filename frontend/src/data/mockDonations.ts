import { Donation } from '../types/types';

export const mockDonations: Donation[] = [
  {
    id: '1',
    amount: 100,
    currency: 'USD',
    ngo: 'Save the Children',
    date: '2023-05-01',
    project: 'Education in Africa',
    status: [
      { status: 'Donated', date: '2023-05-01', description: 'Donation received' },
      { status: 'Processed', date: '2023-05-03', description: 'Donation processed by NGO' },
      { status: 'Allocated', date: '2023-05-10', description: 'Funds allocated to project' },
      { status: 'Used', date: '2023-05-15', description: 'Funds being used for project' },
    ],
  },
  {
    id: '2',
    amount: 50,
    currency: 'EUR',
    ngo: 'Red Cross',
    date: '2023-06-15',
    project: 'Disaster Relief',
    status: [
      { status: 'Donated', date: '2023-06-15', description: 'Donation received' },
      { status: 'Processed', date: '2023-06-17', description: 'Donation processed by NGO' },
      { status: 'Allocated', date: '2023-06-20', description: 'Funds allocated to project' },
    ],
  },
  // Add more mock donations as needed
];

