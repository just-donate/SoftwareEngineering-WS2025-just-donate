'use client';

import { useState, useEffect } from 'react';
import { DonationWithDonor } from '@/types/types';
import {
  fetchDonations,
  createDonation,
} from '@/app/organization/donations/donations';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '@/components/organization/ui/card';
import { Input } from '@/components/organization/ui/input';
import { Button } from '@/components/organization/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/organization/ui/select';
import DonationsList from '@/components/organization/DonationsList';
import { BankAccount, Earmarking } from '@/types/types';
import { fetchBankAccounts } from '@/app/organization/bank-accounts/bank-accounts';
import { fetchEarmarkings } from '@/app/organization/earmarkings/earmarkings';
import { useTheme } from '@/contexts/ThemeContext';

interface DonationManagerProps {
  initialDonations: DonationWithDonor[];
  organizationId: string;
}

export default function DonationManager({
  initialDonations,
  organizationId,
}: DonationManagerProps) {
  const { theme } = useTheme();
  const [donations, setDonations] =
    useState<DonationWithDonor[]>(initialDonations);
  const [aviableAccounts, setAviableAccounts] = useState<BankAccount[]>([]);
  const [aviableEarmarkings, setAviableEarmarkings] = useState<Earmarking[]>(
    [],
  );
  const [donorName, setDonorName] = useState('');
  const [donorEmail, setDonorEmail] = useState('');
  const [amount, setAmount] = useState('');
  const [earmarking, setEarmarking] = useState('');
  const [accountName, setAccountName] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    fetchDonations(organizationId).then(setDonations);
    fetchBankAccounts(organizationId).then(setAviableAccounts);
    fetchEarmarkings(organizationId).then(setAviableEarmarkings);
  }, [organizationId]);

  const addDonation = async () => {
    const newDonation = {
      donorName,
      donorEmail,
      amount: { amount: amount },
      earmarking: earmarking === '' ? null : earmarking,
      accountName,
    };

    if (
      !newDonation.donorName ||
      !newDonation.donorEmail ||
      !newDonation.amount.amount ||
      !newDonation.accountName
    ) {
      setErrorMessage('Please fill in all fields');
      return;
    }

    const result = await createDonation(
      organizationId,
      newDonation.donorName,
      newDonation.donorEmail,
      newDonation.amount,
      newDonation.earmarking,
      newDonation.accountName,
    );
    if (result.success) {
      setSuccessMessage('Donation created successfully');
      setErrorMessage('');
      fetchDonations(organizationId).then(setDonations);
    } else {
      setErrorMessage(result.error || 'Failed to create donation');
      setSuccessMessage('');
    }
  };

  return (
    <div>
      <Card className={theme.card}>
        <CardHeader className="mb-4">
          <CardTitle className={theme.text}>Donations</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex flex-col space-y-2">
              <Input
                value={donorName}
                onChange={(e) => setDonorName(e.target.value)}
                placeholder="Donor Name"
                className={`${theme.background} ${theme.text}`}
                data-testid="donor-name-input"
              />
              <Input
                value={donorEmail}
                onChange={(e) => setDonorEmail(e.target.value)}
                placeholder="Donor Email"
                className={`${theme.background} ${theme.text}`}
                data-testid="donor-email-input"
              />
              <Input
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="Amount"
                className={`${theme.background} ${theme.text}`}
                data-testid="amount-input"
              />
              <Select value={earmarking} onValueChange={setEarmarking}>
                <SelectTrigger className={`${theme.background} ${theme.text}`}>
                  <SelectValue placeholder="Select Earmarking" />
                </SelectTrigger>
                <SelectContent className={theme.card}>
                  {aviableEarmarkings.map((earmarking) => (
                    <SelectItem key={earmarking.name} value={earmarking.name}>
                      {earmarking.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select value={accountName} onValueChange={setAccountName}>
                <SelectTrigger className={`${theme.background} ${theme.text}`}>
                  <SelectValue placeholder="Select Account" />
                </SelectTrigger>
                <SelectContent className={theme.card}>
                  {aviableAccounts.map((account) => (
                    <SelectItem key={account.name} value={account.name}>
                      {account.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button onClick={addDonation} className={theme.primary}>
                Create Donation
              </Button>
              {successMessage && (
                <p className="text-green-500">{successMessage}</p>
              )}
              {errorMessage && <p className="text-red-500">{errorMessage}</p>}
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="mt-4">
        <DonationsList initialDonations={donations} />
      </div>
    </div>
  );
}
