'use client';

import React, {
  createContext,
  useState,
  useContext,
  ReactNode,
  useEffect,
} from 'react';
import { Theme, themes } from '@/styles/themes';
import axios from 'axios';
import axiosInstance from '@/app/organization/api/axiosInstance';

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

export async function getTheme(organizationId: string): Promise<Theme | null> {
  try {
    const response = await axiosInstance.get<Theme>(
      `public/organisation/${organizationId}/theme`,
    );
    if (response.data) {
      return response.data;
    } else {
      return null;
    }
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 404) {
        return null;
      }
      throw new Error(`Failed to fetch theme: ${error.response?.statusText}`);
    }
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

    await axiosInstance.post(`/organisation/${organizationId}/theme`, theme, {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // No need to check response.ok; Axios throws an error for non-2xx statuses.
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
      typeof theme.statusColors.used === 'string' &&
      typeof theme.emailTemplates === 'object' &&
      typeof theme.emailTemplates.donationTemplate === 'string' &&
      typeof theme.emailTemplates.transferTemplate === 'string' &&
      typeof theme.emailTemplates.withdrawalTemplate === 'string'
    );
  } catch {
    return false;
  }
}
