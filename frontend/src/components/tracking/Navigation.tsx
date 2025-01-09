import React from 'react';
import { useTheme } from '@/contexts/ThemeContext';
import Link from 'next/link';
import Image from 'next/image';
import logo from '@/assets/logo_white_small.png';
import router from 'next/router';

interface NavigationProps {
  links: {
    link: string;
    name: string;
  }[];
}

export const Navigation: React.FC<NavigationProps> = ({ links }) => {
  const { theme } = useTheme();

  const clickLink = (link: string) => {
    router.push(link);
  }

  return (
      <nav className={`${theme.primary} shadow`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center">
              <Link href="/" className="flex items-center hover:opacity-80 transition-opacity">
                <Image src={logo} alt="Logo" className="h-8 w-8 mr-2 object-contain" />
                <h1 className={`text-2xl font-bold text-white`}>Donation Tracker</h1>
              </Link>
            </div>
            <div className="ml-auto flex space-x-4">
              {links.map((link, index) => (
                  <Link
                      key={index}
                      href={link.link}
                      className={`text-white hover:opacity-80 transition-opacity`}
                      onClick={() => clickLink(link.link)}
                  >
                    {link.name}
                  </Link>
              ))}
            </div>
          </div>
        </div>
      </nav>
  );
};

