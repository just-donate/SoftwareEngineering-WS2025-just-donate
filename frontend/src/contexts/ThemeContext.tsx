'use client';

import React, {
  createContext,
  useState,
  useContext,
  ReactNode,
  useEffect,
  useCallback,
} from 'react';
import { Theme, themes } from '@/styles/themes';
import axios from 'axios';
import axiosInstance from '@/app/organization/api/axiosInstance';
import { config } from '@/lib/config';

interface ThemeContextType {
  theme: Theme;
  updateTheme: (newTheme: Theme) => Promise<void>;
  isLoading: boolean;
}

interface CachedTheme {
  theme: Theme;
  timestamp: number;
  organizationId: string;
}

const organizationId = config.organizationId;
const THEME_STORAGE_KEY = `theme_${organizationId}`;
const CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [theme, setThemeState] = useState<Theme | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const getCachedTheme = useCallback((): Theme | null => {
    if (
      typeof window === 'undefined' ||
      typeof window.localStorage === 'undefined'
    )
      return null;

    const storedData = window.localStorage.getItem(THEME_STORAGE_KEY);
    if (!storedData) return null;

    try {
      const cached: CachedTheme = JSON.parse(storedData);
      const isExpired = Date.now() - cached.timestamp > CACHE_DURATION;
      const isValidOrg = cached.organizationId === organizationId;

      if (isExpired || !isValidOrg || !isValidTheme(cached.theme)) {
        window.localStorage.removeItem(THEME_STORAGE_KEY);
        return null;
      }

      return cached.theme;
    } catch {
      return null;
    }
  }, []);

  const setCachedTheme = useCallback((newTheme: Theme) => {
    const cacheData: CachedTheme = {
      theme: newTheme,
      timestamp: Date.now(),
      organizationId,
    };
    window.localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(cacheData));
  }, []);

  const fetchTheme = useCallback(async () => {
    try {
      const savedTheme = await getTheme(organizationId);
      if (savedTheme) {
        setThemeState(savedTheme);
        setCachedTheme(savedTheme);
      } else {
        setThemeState(themes.default);
        window.localStorage.removeItem(THEME_STORAGE_KEY);
      }
    } catch {
      setThemeState(themes.default);
    } finally {
      setIsLoading(false);
    }
  }, [setCachedTheme]);

  const updateTheme = useCallback(
    async (newTheme: Theme) => {
      setIsLoading(true);
      try {
        const result = await saveTheme(organizationId, newTheme);

        if (!result.success) {
          throw new Error(result.error || 'Failed to save theme');
        }

        setThemeState(newTheme);
        setCachedTheme(newTheme);
      } catch (error) {
        console.error('Failed to update theme:', error);
        throw error instanceof Error
          ? error
          : new Error('Failed to update theme');
      } finally {
        setIsLoading(false);
      }
    },
    [setCachedTheme],
  );

  useEffect(() => {
    const initializeTheme = async () => {
      const cachedTheme = getCachedTheme();
      if (cachedTheme) {
        setThemeState(cachedTheme);
        setIsLoading(false);
        // Fetch in background to update cache
        fetchTheme();
      } else {
        await fetchTheme();
      }
    };

    initializeTheme();
  }, [fetchTheme, getCachedTheme]);

  if (isLoading || !theme) {
    return null;
  }

  return (
    <ThemeContext.Provider value={{ theme, updateTheme, isLoading }}>
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
    return response.data || null;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    // For any other error, return null to use default theme
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
      theme.emailTemplates !== null &&
      typeof theme.emailTemplates.donationTemplate === 'string' &&
      typeof theme.emailTemplates.withdrawalTemplate === 'string' &&
      typeof theme.emailTemplates.manualTemplate === 'string'
    );
  } catch {
    return false;
  }
}
