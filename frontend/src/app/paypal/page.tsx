'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import logo from '@/assets/logo.png';
// Ensure axiosInstance is properly configured and imported
import axiosInstance from '../organization/api/axiosInstance';
import axios from 'axios';

interface Purpose {
  name: string;
}

const DonationPage: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [amount, setAmount] = useState('');
  const [purpose, setPurpose] = useState('None');
  const [errorMessage, setErrorMessage] = useState('');
  const [purposeOptions, setPurposeOptions] = useState<string[]>([]);

  /**
   * Name of the created organisation
   */
  const orgName = 'Just-Donate';

  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';

  /**
   * Fetch purpose options from the endpoint on component mount.
   */
  useEffect(() => {
    const fetchPurposeOptions = async () => {
      try {
        const response = await axiosInstance.get<Purpose[]>(
          `organisation/${organizationId}/earmarking/list`,
        );
        // Extract purpose names from the response data
        const options = response.data.map((item) => item.name);
        setPurposeOptions(options);
      } catch (error: any) {
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
    // Remove invalid characters
    let formatted = value.replace(/[^0-9.]/g, '');

    // Limit to two decimal places
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

    // Simulate PayPal form submission
    const paypalForm = document.createElement('form');
    paypalForm.method = 'POST';
    paypalForm.action = 'https://www.sandbox.paypal.com/cgi-bin/webscr';

    paypalForm.innerHTML = `
      <input type="hidden" name="cmd" value="_xclick" />
      <input type="hidden" name="business" value="sb-swv8l35388930@business.example.com" />
      <input type="hidden" name="item_name" value="${purpose}" />
      <input type="hidden" name="currency_code" value="EUR" />
      <input type="hidden" name="amount" value="${parsedAmount.toFixed(2)}" />
      <input type="hidden" name="return" value="https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/" />
      <input type="hidden" name="cancel_return" value="https://just-donate.github.io/SoftwareEngineering-WS2025-just-donate/" />
      <input type="hidden" name="invoice" value="${invoice}" />
      <input type="hidden" name="custom" value="${orgName}" />
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
              htmlFor="name"
              className="block text-gray-700 font-medium mb-1"
            >
              Name
            </label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Max Mustermann"
              className="w-full border border-gray-300 rounded-md p-2"
            />
          </div>

          <div className="mb-4">
            <label
              htmlFor="email"
              className="block text-gray-700 font-medium mb-1"
            >
              Email <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="mustermann@beispiel.com"
              required
              className="w-full border border-gray-300 rounded-md p-2"
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
              <span className="absolute left-3 top-2.5 text-gray-500">$</span>
              <input
                type="text"
                id="amount"
                value={amount}
                onChange={(e) => formatAmount(e.target.value)}
                placeholder="0.00"
                required
                className="w-full pl-8 border border-gray-300 rounded-md p-2"
              />
              <datalist id="amounts">
                <option value="5" />
                <option value="25" />
                <option value="50" />
                <option value="100" />
              </datalist>
            </div>
          </div>

          <div className="mb-4">
            <label
              htmlFor="purpose"
              className="block text-gray-700 font-medium mb-1"
            >
              Donation Purpose
            </label>
            <select
              id="purpose"
              value={purpose}
              onChange={(e) => setPurpose(e.target.value)}
              className="w-full border border-gray-300 rounded-md p-2"
            >
              {/* Default option that can be selected */}
              <option value="None">Choose a purpose (or clear selection)</option>
              {purposeOptions.length > 0 ? (
                purposeOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))
              ) : (
                <option value="">Loading options...</option>
              )}
            </select>
          </div>

          <button
            type="submit"
            className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
          >
            Donate Now
          </button>
        </form>

        <p className="text-center text-gray-500 text-sm mt-6">
          powered by <Image src={logo} alt="just-donate-logo" />
        </p>
      </div>
    </div>
  );
};

export default DonationPage;
