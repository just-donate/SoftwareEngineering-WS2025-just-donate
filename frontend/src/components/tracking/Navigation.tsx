'use client'

import React from 'react';
import { useTheme } from '../../contexts/ThemeContext';
import { ThemeSwitcher } from './ThemeSwitcher';
import { themes } from '../../styles/themes';


export const Navigation: React.FC = () => {
  const { theme } = useTheme();

  const getCurrentThemeName = () => {
    return Object.keys(themes).find(key => themes[key] === theme) || 'Default';
  };

  // bg-purple-600 text-white

  return (
    <nav className={`${theme.primary} shadow`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center">
            <span className="text-2xl mr-2" role="img" aria-label="Logo">
              {theme.icon}
            </span>
            <h1 className={`text-2xl font-bold text-white`}>Donation Tracker</h1>
          </div>
          <div className="flex items-center space-x-4">
            <a
              href={theme.ngoUrl}
              target="_blank"
              rel="noopener noreferrer"
              className={`text-white hover:opacity-80 transition-opacity`}
            >
              {theme.ngoName}
            </a>
            <a
              href={theme.helpUrl}
              className={`text-white hover:opacity-80 transition-opacity`}
            >
              Help
            </a>
            <span className="text-white">Current Theme: {getCurrentThemeName()}</span>
            <ThemeSwitcher />
          </div>
        </div>
      </div>
    </nav>
  );
};

