'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import { ToastContainer, toast } from 'react-toastify';
import { useTheme } from '@/contexts/ThemeContext';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { theme } = useTheme();
  const orgId = '591671920';
  const API_URL = process.env.NEXT_PUBLIC_API_URL;

  if (!API_URL) {
    throw new Error('NEXT_PUBLIC_API_URL is not set');
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);

    try {
      // Perform the login request
      await axios.post(
        `${API_URL}/login`, // Replace with your login endpoint
        {
          username: email,
          password: password,
          orgId: orgId,
        },
        {
          // Ensure cookies are sent and stored
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json', // Specify JSON content type
          },
        },
      );

      // On success, display a success notification (optional) and redirect to dashboard
      toast.success('Login successful!', {
        position: 'top-center',
      });
      router.push('/organization/dashboard');
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
