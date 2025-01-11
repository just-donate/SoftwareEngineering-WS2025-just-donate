'use client';

import React, {
  createContext,
  useState,
  useContext,
  ReactNode,
  useEffect,
} from 'react';
import { Theme, themes } from '@/styles/themes';

interface ThemeContextType {
  theme: Theme;
  updateTheme: (newTheme: Theme) => Promise<void>;
}

const organizationId = '591671920';

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [theme, setThemeState] = useState<Theme>(themes.default);

  const fetchTheme = async () => {
    try {
      const savedTheme = await getTheme(organizationId);
      if (savedTheme) {
        setThemeState(savedTheme);
      }
    } catch (error) {
      console.error('Failed to fetch theme:', error);
    }
  };

  const updateTheme = async (newTheme: Theme) => {
    try {
      const result = await saveTheme(organizationId, newTheme);

      if (!result.success) {
        throw new Error(result.error || 'Failed to save theme');
      }

      setThemeState(newTheme);
    } catch (error) {
      console.error('Failed to update theme:', error);
      throw error instanceof Error
        ? error
        : new Error('Failed to update theme');
    }
  };

  useEffect(() => {
    fetchTheme();
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, updateTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

export async function getTheme(organizationId: string): Promise<Theme | null> {
  try {
    const response = await fetch(
      `${API_URL}/organisation/${organizationId}/theme`,
      {
        method: 'GET',
      },
    );

    if (!response.ok) {
      if (response.status === 404) {
        return null;
      }
      throw new Error(`Failed to fetch theme: ${response.statusText}`);
    }

    const themeData = await response.text();
    return JSON.parse(themeData);
  } catch (error) {
    console.error('Failed to fetch theme:', error);
    return null;
  }
}

export async function saveTheme(organizationId: string, theme: Theme) {
  try {
    // Validate theme structure
    if (!isValidTheme(theme)) {
      throw new Error('Invalid theme structure');
    }

    const response = await fetch(
      `${API_URL}/organisation/${organizationId}/theme`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(theme),
      },
    );

    if (!response.ok) {
      throw new Error(`Failed to update theme: ${response.statusText}`);
    }

    return { success: true };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Failed to save theme',
    };
  }
}

export function isValidTheme(theme: Partial<Theme>): theme is Theme {
  if (!theme || typeof theme !== 'object') return false;

  try {
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
      theme.statusColors !== null &&
      typeof theme.statusColors.announced === 'string' &&
      typeof theme.statusColors.pending_confirmation === 'string' &&
      typeof theme.statusColors.confirmed === 'string' &&
      typeof theme.statusColors.received === 'string' &&
      typeof theme.statusColors.in_transfer === 'string' &&
      typeof theme.statusColors.processing === 'string' &&
      typeof theme.statusColors.allocated === 'string' &&
      typeof theme.statusColors.awaiting_utilization === 'string' &&
      typeof theme.statusColors.used === 'string'
    );
  } catch {
    return false;
  }
}
