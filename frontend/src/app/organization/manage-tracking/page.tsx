'use client'

import React, { useState, useEffect } from 'react'
import { Button } from "../../../components/organization/ui/button"
import { Card, CardHeader, CardTitle, CardContent } from "../../../components/organization/ui/card"
import { Theme, themes } from '../../../styles/themes'

export default function ManageTrackingPage() {
  const [theme, setTheme] = useState<Theme | null>(null)
  const [themeString, setThemeString] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('') // State for success message

  useEffect(() => {
    // Mock API call to fetch the current theme
    const fetchedTheme = themes.default; // Replace with the actual theme fetching logic
    setTheme(fetchedTheme);
    setThemeString(formatThemeToString(fetchedTheme)); // Initialize input with the current theme as a formatted string
  }, []);

  const formatThemeToString = (theme: Theme): string => {
    return JSON.stringify(theme, null, 2); // Format the theme as a pretty-printed JSON string
  }

  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setThemeString(e.target.value);
  }

  const handleSave = () => {
    try {
      // Validate and parse the theme string
      const parsedTheme = JSON.parse(themeString);

      // Check if the parsed object matches the Theme interface
      if (isValidTheme(parsedTheme)) {
        console.log('Updated Theme:', parsedTheme);
        // TODO: Send theme to server
        setError(''); // Clear any previous errors
        setSuccessMessage('Theme successfully saved!'); // Set success message
        setTimeout(() => setSuccessMessage(''), 3000); // Clear message after 3 seconds
      } else {
        throw new Error('Invalid theme structure');
      }
    } catch (err) {
      setError('Invalid theme format. Please enter a valid JSON string.');
      setSuccessMessage(''); // Clear success message on error
    }
  }

  const isValidTheme = (theme: any): theme is Theme => {
    // Basic validation to check if the theme has the required properties
    return (
      typeof theme.primary === 'string' &&
      typeof theme.secondary === 'string' &&
      typeof theme.accent === 'string' &&
      typeof theme.background === 'string' &&
      typeof theme.card === 'string' &&
      typeof theme.text === 'string' &&
      typeof theme.textLight === 'string' &&
      typeof theme.font === 'string' &&
      typeof theme.icon === 'string' &&
      typeof theme.ngoName === 'string' &&
      typeof theme.ngoUrl === 'string' &&
      typeof theme.helpUrl === 'string' &&
      typeof theme.statusColors === 'object' &&
      typeof theme.statusColors.donated === 'string' &&
      typeof theme.statusColors.inTransit === 'string' &&
      typeof theme.statusColors.allocated === 'string' &&
      typeof theme.statusColors.used === 'string'
    );
  }

  if (!theme) return <div>Loading...</div>;

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
            {successMessage && <div className="text-green-500">{successMessage}</div>} {/* Success message alert */}
            <Button onClick={handleSave}>Save</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}