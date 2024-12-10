import { Donation } from "@/types/types";

export function getDonations(id: string): Donation[] | null {
    return mockDonations;
    // return fetch(`/api/tracking/${id}`).then(body => body.json()).catch(_ => null);
}

export const mockDonations: Donation[] = [
    {
      id: '1',
      amount: 100,
      currency: 'USD',
      ngo: 'Save the Children',
      donorEmail: 'john.doe@example.com',
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
      donorEmail: 'jane.smith@example.com',
      date: '2023-06-15',
      project: 'Disaster Relief',
      status: [
        { status: 'Donated', date: '2023-06-15', description: 'Donation received' },
        { status: 'Processed', date: '2023-06-17', description: 'Donation processed by NGO' },
        { status: 'Allocated', date: '2023-06-20', description: 'Funds allocated to project' },
      ],
    },
    {
      id: '3',
      amount: 75,
      currency: 'GBP',
      ngo: 'Oxfam',
      donorEmail: 'john.doe@example.com',
      date: '2023-07-20',
      project: 'Clean Water Initiative',
      status: [
        { status: 'Donated', date: '2023-07-20', description: 'Donation received' },
        { status: 'Processed', date: '2023-07-22', description: 'Donation processed by NGO' },
      ],
    },
    {
      id: '4',
      amount: 200,
      currency: 'USD',
      ngo: 'UNICEF',
      donorEmail: 'john.doe@example.com',
      date: '2023-08-05',
      project: 'Child Healthcare',
      status: [
        { status: 'Donated', date: '2023-08-05', description: 'Donation received' },
        { status: 'Processed', date: '2023-08-07', description: 'Donation processed by NGO' },
        { status: 'Allocated', date: '2023-08-10', description: 'Funds allocated to project' },
        { status: 'Used', date: '2023-08-15', description: 'Funds being used for project' },
        { status: 'Completed', date: '2023-09-01', description: 'Project goals achieved' },
      ],
    },
    {
      id: '5',
      amount: 150,
      currency: 'CAD',
      ngo: 'WWF',
      donorEmail: 'john.doe@example.com',
      date: '2023-09-10',
      project: 'Wildlife Conservation',
      status: [
        { status: 'Donated', date: '2023-09-10', description: 'Donation received' },
        { status: 'Processed', date: '2023-09-12', description: 'Donation processed by NGO' },
        { status: 'Allocated', date: '2023-09-15', description: 'Funds allocated to project' },
      ],
    }
  ];
  
  