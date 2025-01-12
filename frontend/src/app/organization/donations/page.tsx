'use client';

import { useState, useEffect } from 'react';
import DonationsList from '@/components/organization/DonationsList';
import { Donation } from '@/types/types';
import axiosInstance from '../api/axiosInstance';
import { fetchDonations } from './donations';

export default function DonationsPage() {
  const [donations, setDonations] = useState<Donation[]>([]);

  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  const [newDonation, setNewDonation] = useState({
    donorName: '',
    donorEmail: '',
    amount: '',
    earmarking: '',
    accountName: '',
  });
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    fetchDonations(organizationId).then(setDonations);
  }, [organizationId]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setNewDonation((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axiosInstance.post(`/donate/${organizationId}/account/${newDonation.accountName}`, {
        donorName: newDonation.donorName,
        donorEmail: newDonation.donorEmail,
        amount: { amount: newDonation.amount }, // Wrap amount in Money object
        earmarking: newDonation.earmarking,
      });
      // Load the donations again to get the right status
      fetchDonations(organizationId).then(setDonations);
      setSuccessMessage('Donation created successfully!');
      setErrorMessage('');
      setNewDonation({ donorName: '', donorEmail: '', amount: '', earmarking: '', accountName: '' }); // Reset form
    } catch (error) {
      setErrorMessage('Failed to create donation. Please try again.');
      setSuccessMessage('');
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Donations</h1>
      <form onSubmit={handleSubmit} className="mb-4">
        <input
          type="text"
          name="donorName"
          placeholder="Donor Name"
          value={newDonation.donorName}
          onChange={handleInputChange}
          required
          className="border p-2 mb-2"
        />
        <input
          type="email"
          name="donorEmail"
          placeholder="Donor Email"
          value={newDonation.donorEmail}
          onChange={handleInputChange}
          required
          className="border p-2 mb-2"
        />
        <input
          type="number"
          name="amount"
          placeholder="Amount"
          value={newDonation.amount}
          onChange={handleInputChange}
          required
          className="border p-2 mb-2"
        />
        <input
          type="text"
          name="earmarking"
          placeholder="Earmarking"
          value={newDonation.earmarking}
          onChange={handleInputChange}
          className="border p-2 mb-2"
        />
        <input
          type="text"
          name="accountName"
          placeholder="Account Name"
          value={newDonation.accountName}
          onChange={handleInputChange}
          className="border p-2 mb-2"
        />
        <button type="submit" className="bg-blue-500 text-white p-2">
          Create Donation
        </button>
      </form>
      {successMessage && <p className="text-green-500">{successMessage}</p>}
      {errorMessage && <p className="text-red-500">{errorMessage}</p>}
      <DonationsList initialDonations={donations} />
    </div>
  );
}
