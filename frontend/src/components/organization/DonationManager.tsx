'use client';

import { useState, useEffect } from 'react';
import { Donation } from '@/types/types';
import { fetchDonations, createDonation } from '@/app/organization/donations/donations';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/organization/ui/card';
import { Input } from '@/components/organization/ui/input';
import { Button } from '@/components/organization/ui/button';
import DonationsList from '@/components/organization/DonationsList';


interface DonationManagerProps {
  initialDonations: Donation[];
  organizationId: string;
}

export default function DonationManager({
    initialDonations,
    organizationId,
}: DonationManagerProps) {
    const [donations, setDonations] = useState<Donation[]>(initialDonations);
    const [donorName, setDonorName] = useState('');
    const [donorEmail, setDonorEmail] = useState('');
    const [amount, setAmount] = useState('');
    const [earmarking, setEarmarking] = useState('');
    const [accountName, setAccountName] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        fetchDonations(organizationId).then(setDonations);
    }, [organizationId]);

    const addDonation = async () => {
        const newDonation = {
            donorName,
            donorEmail,
            amount: { amount: amount },
            earmarking,
            accountName,
        }

        if (!newDonation.donorName || !newDonation.donorEmail || !newDonation.amount.amount || !newDonation.accountName) {
            setErrorMessage('Please fill in all fields');
            return;
        }

        const result = await createDonation(organizationId, newDonation.donorName, newDonation.donorEmail, newDonation.amount, newDonation.earmarking, newDonation.accountName);
        if (result.success) {
            setSuccessMessage('Donation created successfully');
            setErrorMessage('');
            fetchDonations(organizationId).then(setDonations);
        } else {
            setErrorMessage(result.error || 'Failed to create donation');
            setSuccessMessage('');
        }
    }

    return (
    <div>
      <Card className="mb-4">
        <CardHeader className="mb-4">
          <CardTitle>Donations</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col space-y-2">
              <Input
                value={donorName}
                onChange={(e) => setDonorName(e.target.value)}
                placeholder="Donor Name"
              />
              <Input
                value={donorEmail}
                onChange={(e) => setDonorEmail(e.target.value)}
                placeholder="Donor Email"
              />
              <Input
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="Amount"
              />
              <Input
                value={earmarking}
                onChange={(e) => setEarmarking(e.target.value)}
                placeholder="Earmarking"
              />
              <Input
                value={accountName}
                onChange={(e) => setAccountName(e.target.value)}
                placeholder="Account Name"
              />
              <Button onClick={addDonation}>Create Donation</Button>
              {successMessage && <p className="text-green-500">{successMessage}</p>}
              {errorMessage && <p className="text-red-500">{errorMessage}</p>}
            </div>
          </div>
        </CardContent>
      </Card>

      <DonationsList initialDonations={donations} />
    </div>
    )
}

