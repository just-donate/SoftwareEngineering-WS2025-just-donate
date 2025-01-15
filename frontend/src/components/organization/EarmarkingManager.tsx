'use client';

import { useState, useEffect } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Textarea } from './ui/textarea';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Earmarking } from '@/types/types';
import { useTheme } from '@/contexts/ThemeContext';
import axiosInstance from '@/app/organization/api/axiosInstance';
import axios from 'axios';

interface EarmarkingManagerProps {
  initialEarmarkings: Earmarking[];
  organizationId: string;
}

export default function EarmarkingManager({
  initialEarmarkings,
  organizationId,
}: EarmarkingManagerProps) {
  const { theme } = useTheme();
  const [earmarkings, setEarmarkings] =
    useState<Earmarking[]>(initialEarmarkings);
  const [newEarmarkingName, setNewEarmarkingName] = useState('');
  const [newEarmarkingDescription, setNewEarmarkingDescription] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    setEarmarkings(initialEarmarkings);
  }, [initialEarmarkings]);

  const addEarmarking = async () => {
    if (!newEarmarkingName) return;

    try {
      await axiosInstance.post(
        `/organisation/${organizationId}/earmarking`,
        {
          name: newEarmarkingName,
          description: newEarmarkingDescription,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );

      // Optimistically update the UI
      const newEarmarking: Earmarking = {
        name: newEarmarkingName,
        description: newEarmarkingDescription,
      };
      setEarmarkings([...earmarkings, newEarmarking]);
      setNewEarmarkingName('');
      setNewEarmarkingDescription('');
      setSuccessMessage('Earmarking created successfully!');
      setError('');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      let errorMessage = 'Failed to create earmarking';

      if (axios.isAxiosError(error)) {
        // Narrowed type: AxiosError
        errorMessage =
          error.response?.data?.error || error.message || errorMessage;
      } else if (error instanceof Error) {
        // Standard JS Error
        errorMessage = error.message;
      }

      setError(errorMessage);
      setSuccessMessage('');
    }
  };

  return (
    <div>
      <Card className={`mb-4 ${theme.card}`}>
        <CardHeader>
          <CardTitle className={theme.text}>Add New Earmarking</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <Input
                value={newEarmarkingName}
                onChange={(e) => setNewEarmarkingName(e.target.value)}
                placeholder="Earmarking name"
                className={`${theme.background} ${theme.text}`}
              />
              <Textarea
                value={newEarmarkingDescription}
                onChange={(e) => setNewEarmarkingDescription(e.target.value)}
                placeholder="Description"
                className="min-h-[100px] resize-none"
              />
              <Button
                onClick={addEarmarking}
                className={`${theme.primary} flex justify-start`}
              >
                Add
              </Button>
            </div>
            {error && <div className="text-red-500">{error}</div>}
            {successMessage && (
              <div className="text-green-500">{successMessage}</div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card className={theme.card}>
        <CardHeader>
          <CardTitle className={theme.text}>Existing Earmarkings</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {earmarkings.map((earmarking) => (
              <li
                key={earmarking.name}
                className={`p-2 rounded-lg ${theme.secondary}`}
              >
                <div className={`font-medium ${theme.text}`}>
                  {earmarking.name}
                </div>
                {earmarking.description && (
                  <div
                    className={`text-sm text-muted-foreground ${theme.text}`}
                  >
                    {earmarking.description}
                  </div>
                )}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
