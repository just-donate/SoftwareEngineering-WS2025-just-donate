'use client';

import React, { useState, useEffect } from 'react';
import { Button } from '../../../components/organization/ui/button';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '../../../components/organization/ui/card';
import { Theme } from '../../../styles/themes';
import { isValidTheme, useTheme } from '@/contexts/ThemeContext';
import withAuth from '../api/RequiresAuth';

function ManageTrackingPage() {
  const { theme, updateTheme } = useTheme();
  const [themeString, setThemeString] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    setThemeString(formatThemeToString(theme));
  }, [theme]);

  const formatThemeToString = (theme: Theme): string => {
    return JSON.stringify(theme, null, 2);
  };

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setThemeString(e.target.value);
  };

  const handleSave = async () => {
    try {
      const parsedTheme = JSON.parse(themeString);

      if (isValidTheme(parsedTheme)) {
        await updateTheme(parsedTheme);
        setError('');
        setSuccessMessage('Theme successfully saved!');
        setTimeout(() => setSuccessMessage(''), 3000);
      } else {
        throw new Error('Invalid theme structure');
      }
    } catch (error) {
      console.error(error);
      setError('Invalid theme format. Please enter a valid JSON string.');
      setSuccessMessage('');
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Tracking Site</h1>
      <Card>
        <CardHeader>
          <CardTitle>Edit Theme</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <textarea
              value={themeString}
              onChange={handleChange}
              placeholder="Enter theme as JSON string"
              className="mb-2 w-full h-[60vh] p-2 border rounded"
            />
            {error && <div className="text-red-500">{error}</div>}
            {successMessage && (
              <div className="text-green-500">{successMessage}</div>
            )}
            <Button onClick={handleSave}>Save</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

export default withAuth(ManageTrackingPage);
