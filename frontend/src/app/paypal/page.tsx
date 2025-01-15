'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import logo from '@/assets/logo.png';
import axiosInstance from '../organization/api/axiosInstance';
import axios from 'axios';

// Import shadcn/ui components (adjust imports as needed)
import { Input } from '@/components/organization/ui/input';
import { Button } from '@/components/organization/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/organization/ui/select';

interface Purpose {
  name: string;
}

const DonationPage: React.FC = () => {
  const EMPTY_VALUE = '__empty__';

  const [email, setEmail] = useState('');
  const [amount, setAmount] = useState('');
  const [purpose, setPurpose] = useState(EMPTY_VALUE);
  const [errorMessage, setErrorMessage] = useState('');
  const [purposeOptions, setPurposeOptions] = useState<string[]>([]);

  // Organization Info
  const orgName = 'Just-Donate';
  const organizationId = '591671920';

  // Fetch purpose options on mount
  useEffect(() => {
    const fetchPurposeOptions = async () => {
      try {
        const response = await axiosInstance.get<Purpose[]>(
          `public/organisation/${organizationId}/earmarking/list`,
        );
        const options = response.data.map((item) => item.name);
        setPurposeOptions(options);
      } catch (error) {
        if (axios.isAxiosError(error)) {
          console.error(
            'Axios error fetching purposes:',
            error.response || error,
          );
        } else {
          console.error('Unexpected error:', error);
        }
        setErrorMessage(
          'Unable to load donation purposes. Please try again later.',
        );
      }
    };

    fetchPurposeOptions();
  }, [organizationId]);

  const formatAmount = (value: string) => {
    let formatted = value.replace(/[^0-9.]/g, '');
    if (formatted.includes('.')) {
      const parts = formatted.split('.');
      parts[1] = parts[1].slice(0, 2);
      formatted = parts.join('.');
    }
    setAmount(formatted);
  };

  const validateAndSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setErrorMessage('Please enter a valid donation amount greater than 0.');
      return;
    }
    setErrorMessage('');
    const invoice = Math.floor(Math.random() * 1000000000);

    // Create and submit a PayPal form programmatically
    const paypalForm = document.createElement('form');
    paypalForm.method = 'POST';
    paypalForm.action = 'https://www.sandbox.paypal.com/cgi-bin/webscr';
    paypalForm.innerHTML = `
      <input type="hidden" name="cmd" value="_xclick" />
      <input type="hidden" name="business" value="sb-8rsvi36693121@business.example.com" />
      <input type="hidden" name="item_name" value="${purpose}" />
      <input type="hidden" name="currency_code" value="EUR" />
      <input type="hidden" name="amount" value="${parsedAmount.toFixed(2)}" />
      <input type="hidden" name="return" value="https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/" />
      <input type="hidden" name="cancel_return" value="https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/" />
      <input type="hidden" name="invoice" value="${invoice}" />
      <input type="hidden" name="custom" value="${orgName}/${email}" />
    `;
    document.body.appendChild(paypalForm);
    paypalForm.submit();
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="bg-white max-w-md w-full shadow-md rounded-lg p-6">
        <h2 className="text-xl font-bold text-center text-gray-800 mb-4">
          Make a Difference with Your Donation
        </h2>
        <p className="text-center text-gray-600 mb-6">
          Your contribution brings positive change to many lives. Join us in
          making a difference.
        </p>

        {errorMessage && (
          <p className="text-red-500 text-sm mb-4">{errorMessage}</p>
        )}

        <form onSubmit={validateAndSubmit}>
          <div className="mb-4">
            <label
              htmlFor="email"
              className="block text-gray-700 font-medium mb-1"
            >
              Email <span className="text-red-500">*</span>
            </label>
            <Input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="mustermann@beispiel.com"
              required
              className="w-full"
            />
          </div>

          <div className="mb-4">
            <label
              htmlFor="amount"
              className="block text-gray-700 font-medium mb-1"
            >
              Donation Amount <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                €
              </span>
              <Input
                type="text"
                id="amount"
                value={amount}
                onChange={(e) => formatAmount(e.target.value)}
                placeholder="0.00"
                required
                className="w-full pl-8 placeholder-gray-400"
              />
            </div>
          </div>

          <div className="mb-4">
            <label
              htmlFor="purpose"
              className="block text-gray-700 font-medium mb-1"
            >
              Donation Purpose
            </label>
            <Select
              value={purpose}
              onValueChange={(value) => setPurpose(value)}
            >
              <SelectTrigger className="w-full">
                <SelectValue
                  placeholder="Choose a purpose (or clear selection)"
                  className="placeholder-gray-400"
                />
              </SelectTrigger>
              <SelectContent className="bg-white">
                {/* Use the non-empty special value */}
                <SelectItem
                  value={EMPTY_VALUE}
                  className="bg-white hover:bg-gray-100 focus:bg-gray-200"
                >
                  Choose a purpose (or clear selection)
                </SelectItem>
                {purposeOptions.map((option) => (
                  <SelectItem
                    key={option}
                    value={option}
                    className="bg-white hover:bg-gray-100 focus:bg-gray-200"
                  >
                    {option}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <Button
            type="submit"
            className="w-full bg-blue-500 hover:bg-blue-600"
          >
            Donate Now
          </Button>
        </form>

        <p className="text-center text-gray-500 text-sm mt-6">
          powered by{' '}
          <span className="inline-block">
            <Image src={logo} alt="just-donate-logo" width={120} height={40} />
          </span>
        </p>
      </div>
    </div>
  );
};

export default DonationPage;
