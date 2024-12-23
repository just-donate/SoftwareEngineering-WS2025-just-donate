'use client'

import React from 'react';
import { useTheme } from '../../contexts/ThemeContext';
import Link from 'next/link';

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
              <Link href="/" className="flex items-center hover:opacity-80 transition-opacity">
              <span className="text-2xl mr-2" role="img" aria-label="Logo">
                {theme.icon}
              </span>
                <h1 className={`text-2xl font-bold text-white`}>Donation Tracker</h1>
              </Link>
            </div>
            <div className="ml-auto flex space-x-4">
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
          </div>
        </div>
      </nav>
  );
};

