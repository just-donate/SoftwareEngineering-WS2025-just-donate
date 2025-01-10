'use server';

import { revalidatePath } from 'next/cache';
import { Theme } from '@/styles/themes';
import fs from 'fs/promises';

const THEME_FILE_PATH = './src/styles/saved_theme.json';

export async function getTheme(): Promise<Theme | null> {
  try {
    const themeData = await fs.readFile(THEME_FILE_PATH, 'utf-8');
    return JSON.parse(themeData);
  } catch (error) {
    // If file doesn't exist or there's an error reading it, return null
    console.error('Failed to fetch theme:', error);
    return null;
  }
}

export async function updateTheme(theme: Theme) {
  try {
    // Validate theme structure
    if (!isValidTheme(theme)) {
      throw new Error('Invalid theme structure');
    }

    // Save theme to file
    await fs.writeFile(THEME_FILE_PATH, JSON.stringify(theme, null, 2));

    // Revalidate all pages that might use the theme
    revalidatePath('/');

    return { success: true };
  } catch (error) {
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Failed to save theme',
    };
  }
}

function isValidTheme(theme: Partial<Theme>): theme is Theme {
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
