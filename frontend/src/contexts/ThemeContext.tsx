'use client';

import React, {
  createContext,
  useState,
  useContext,
  ReactNode,
  useEffect,
} from 'react';
import { Theme, themes } from '@/styles/themes';
import {
  getTheme,
  updateTheme as updateThemeAction,
} from '@/app/actions/theme';

interface ThemeContextType {
  theme: Theme;
  updateTheme: (newTheme: Theme) => Promise<void>;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [theme, setThemeState] = useState<Theme>(themes.default);

  const fetchTheme = async () => {
    try {
      const savedTheme = await getTheme();
      if (savedTheme) {
        setThemeState(savedTheme);
      }
    } catch (error) {
      console.error('Failed to fetch theme:', error);
    }
  };

  const updateTheme = async (newTheme: Theme) => {
    try {
      const result = await updateThemeAction(newTheme);

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
