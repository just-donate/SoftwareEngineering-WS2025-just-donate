'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useTheme } from '@/contexts/ThemeContext';
import axiosInstance from '@/app/organization/api/axiosInstance';

const navItems = [
  { href: '/organization/dashboard', label: 'Dashboard' },
  { href: '/organization/earmarkings', label: 'Earmarkings' },
  { href: '/organization/bank-accounts', label: 'Bank Accounts' },
  { href: '/organization/transactions/transfer', label: 'Transfer' },
  { href: '/organization/transactions/withdrawal', label: 'Withdrawal' },
  { href: '/organization/donations', label: 'Donations' },
  { href: '/organization/gallery', label: 'Gallery' },
  { href: '/organization/manage-tracking', label: 'Tracking Page' },
  { href: '/organization/users', label: 'Users' },
];

export const NavBar: React.FC = () => {
  const { theme } = useTheme();
  const pathname = usePathname();
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [windowWidth, setWindowWidth] = useState(0);

  // Check authentication
  useEffect(() => {
    async function checkAuth() {
      try {
        await axiosInstance.get('/check-auth');
        setIsAuthenticated(true);
      } catch {
        setIsAuthenticated(false);
      }
    }
    checkAuth();

    // Update window width on resize
    const handleResize = () => setWindowWidth(window.innerWidth);
    handleResize(); // Initial call
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [pathname]);

  async function logout() {
    try {
      await axiosInstance.post('/logout');
      setIsAuthenticated(false);
      router.push('/organization/login');
    } catch (error) {
      console.error('Error during logout:', error);
    }
  }

  const visibleItems = windowWidth > 1024 ? navItems.slice(0, 5) : [];
  const hiddenItems = windowWidth > 1024 ? navItems.slice(5) : navItems;

  return (
    <nav className={`${theme.primary} shadow-lg`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Left Side: Logo */}
          <div className="flex-shrink-0">
            <Link
              href="/organization/dashboard"
              className={`${theme.text} text-xl font-bold hover:opacity-80 transition-opacity`}
            >
              {theme.ngoName}
            </Link>
          </div>

          {/* Desktop Menu */}
          <div className="hidden lg:flex items-center space-x-6">
            {visibleItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`hover:opacity-80 transition-opacity" ${
                  pathname === item.href ? 'font-bold underline' : ''
                }`}
              >
                {item.label}
              </Link>
            ))}
            {isAuthenticated && (
              <button
                onClick={logout}
                className="px-4 py-2 rounded-md text-sm font-medium text-white hover:text-blue-300 transition-colors"
              >
                Logout
              </button>
            )}
          </div>

          {/* Mobile Menu Toggle */}
          <div className="lg:hidden">
            <button
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              className="text-white focus:outline-none"
            >
              {mobileMenuOpen ? (
                <svg
                  className="h-6 w-6"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              ) : (
                <svg
                  className="h-6 w-6"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div className="lg:hidden bg-gray-800">
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            {hiddenItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setMobileMenuOpen(false)}
                className={`block px-4 py-2 rounded-md text-base font-medium transition-colors ${
                  pathname === item.href
                    ? 'bg-gray-900 text-white'
                    : 'text-gray-300 hover:text-white'
                }`}
              >
                {item.label}
              </Link>
            ))}
            {isAuthenticated && (
              <button
                onClick={() => {
                  setMobileMenuOpen(false);
                  logout();
                }}
                className="block w-full text-left px-4 py-2 rounded-md text-base font-medium text-gray-300 hover:text-white"
              >
                Logout
              </button>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};
