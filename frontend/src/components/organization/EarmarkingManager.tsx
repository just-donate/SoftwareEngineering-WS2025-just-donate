'use client';

import { useState, useEffect } from 'react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Earmarking } from '@/types/types';
import { useTheme } from '@/contexts/ThemeContext';

interface EarmarkingManagerProps {
  initialEarmarkings: Earmarking[];
  organizationId: string;
}

const API_URL = process.env.NEXT_PUBLIC_API_URL;

export default function EarmarkingManager({
  initialEarmarkings,
  organizationId,
}: EarmarkingManagerProps) {
  const { theme } = useTheme();
  const [earmarkings, setEarmarkings] =
    useState<Earmarking[]>(initialEarmarkings);
  const [newEarmarkingName, setNewEarmarkingName] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    setEarmarkings(initialEarmarkings);
  }, [initialEarmarkings]);

  const addEarmarking = async () => {
    if (!newEarmarkingName) return;

    try {
      const response = await fetch(
        `${API_URL}/organisation/${organizationId}/earmarking`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ name: newEarmarkingName }),
        },
      );

      if (!response.ok) {
        throw new Error('Failed to create earmarking');
      }

      // Optimistically update the UI
      const newEarmarking: Earmarking = {
        name: newEarmarkingName,
      };
      setEarmarkings([...earmarkings, newEarmarking]);
      setNewEarmarkingName('');
      setSuccessMessage('Earmarking created successfully!');
      setError('');
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : 'Failed to create earmarking',
      );
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
            <div className="flex space-x-2">
              <Input
                value={newEarmarkingName}
                onChange={(e) => setNewEarmarkingName(e.target.value)}
                placeholder="Earmarking name"
                className={`${theme.background} ${theme.text}`}
              />
              <Button onClick={addEarmarking} className={theme.primary}>
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
                <span className={theme.text}>{earmarking.name}</span>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
