'use client'

import React from 'react';
import { useTheme } from '../../contexts/ThemeContext';
import { ThemeSwitcher } from './ThemeSwitcher';
import { themes } from '../../styles/themes';


interface NavigationProps {
  links: {
    link: string;
    name: string;
  }[];
}

export const Navigation: React.FC<NavigationProps> = ({ links }) => {
  const { theme } = useTheme();

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
          <div className="absolute left-1/2 transform -translate-x-1/2 flex space-x-4">
            {links.map((link, index) => (
              <a
                key={index}
                href={link.link}
                className={`text-white hover:opacity-80 transition-opacity`}
              >
                {link.name}
              </a>
            ))}
          </div>
          <ThemeSwitcher />
        </div>
      </div>
    </nav>
  );
};

