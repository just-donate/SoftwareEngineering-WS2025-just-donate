'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import { ToastContainer, toast } from 'react-toastify';
import { useTheme } from '@/contexts/ThemeContext';
import axiosInstance from '../api/axiosInstance';
import { config } from '@/lib/config';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { theme } = useTheme();
  const orgId = config.organizationId;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);

    try {
      // Perform the login request
      const response = await axiosInstance.post(
        '/login', // Replace with your login endpoint
        {
          username: email,
          password: password,
          orgId: orgId,
        },
        {
          withCredentials: true, // Ensure cookies are sent and stored
          headers: {
            'Content-Type': 'application/json', // Specify JSON content type
          },
        },
      );

      // Retrieve the token from the response
      const token = response.data;

      if (token) {
        // Store the token in localStorage
        sessionStorage.setItem('token', token);

        // Display a success notification (optional)
        toast.success('Login successful!', {
          position: 'top-center',
        });

        // Redirect to the dashboard
        router.push('/organization/dashboard');
      } else {
        throw new Error('No token received from the server.');
      }
    } catch (err) {
      console.error('Login error:', err);

      let message =
        'An unexpected error occurred. Please check your connection.';

      if (axios.isAxiosError(err)) {
        // Now err is narrowed to AxiosError
        if (err.response?.status === 403) {
          message = 'Invalid credentials. Please try again.';
        } else if (err.response?.status === 500) {
          message = 'Server error. Please try again later.';
        }
      }
      toast.error(message, {
        position: 'top-center',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className={`max-w-md mx-auto mt-10 p-6 border border-gray-300 rounded-lg shadow ${theme.card}`}
    >
      <h1
        className={`text-2xl font-bold mb-6 text-center ${theme.text} ${theme.font}`}
      >
        Login
      </h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="email" className={`block mb-1 ${theme.text}`}>
            Email:
          </label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className={`w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 ${theme.text} ${theme.background}`}
          />
        </div>
        <div>
          <label htmlFor="password" className={`block mb-1 ${theme.text}`}>
            Password:
          </label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className={`w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 ${theme.text} ${theme.background}`}
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className={`w-full py-2 rounded transition ${theme.primary}`}
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
      <ToastContainer />
    </div>
  );
}
