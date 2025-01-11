'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '../../../components/organization/ui/button';
import { Input } from '../../../components/organization/ui/input';
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from '../../../components/organization/ui/card';
import axios from 'axios';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const orgId = '591671920';
  const API_URL = process.env.NEXT_PUBLIC_API_URL;

  if (!API_URL) {
    throw new Error('NEXT_PUBLIC_API_URL is not set');
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);

    try {
      // Perform the login request
      const response = await axios.post(
        `${API_URL}/login`, // Replace with your login endpoint
        {
          username: email,
          password: password,
          orgId: orgId,
        },
        {
          // Important options for cookie handling
          withCredentials: true, // Ensure cookies are sent and stored
          headers: {
            'Content-Type': 'application/json', // Specify JSON content type
          },
        },
      );

      // Redirect to the dashboard
      router.push('/organization/dashboard');
    } catch (err: any) {
      console.error('Login error:', err);

      if (err.response?.status === 403) {
        setError('Invalid credentials. Please try again.');
      } else if (err.response?.status === 500) {
        setError('Server error. Please try again later.');
      } else {
        setError('An unexpected error occurred. Please check your connection.');
      }
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen overflow-hidden">
      <Card className="w-[350px] shadow-lg">
        <CardHeader>
          <CardTitle className="text-gray-800">Login</CardTitle>
          <CardDescription className="text-gray-500">
            Enter your credentials to access your account
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent>
            {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
            <div className="grid w-full items-center gap-4">
              <div className="flex flex-col space-y-1.5">
                <Input
                  id="email"
                  type="email"
                  placeholder="Email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="text-gray-800 border border-gray-300"
                />
              </div>
              <div className="flex flex-col space-y-1.5">
                <Input
                  id="password"
                  type="password"
                  placeholder="Password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="text-gray-800 border"
                />
              </div>
            </div>
          </CardContent>
          <CardFooter>
            <Button
              type="submit"
              className="w-full bg-blue-600 text-white hover:bg-blue-700"
            >
              Login
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
