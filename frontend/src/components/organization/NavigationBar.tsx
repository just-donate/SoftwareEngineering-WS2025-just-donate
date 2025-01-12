'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
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
];

export const NavBar: React.FC = () => {
  const { theme } = useTheme();
  const pathname = usePathname();
  const router = useRouter();

  async function logout() {
    try {
      // Call the logout endpoint
      await axiosInstance.post('/logout');

      // Redirect to the login page or home
      router.push('/organization/login');
    } catch (error) {
      console.error(
        'Error during logout:',
        error.response?.data || error.message,
      );
    }
  }

  return (
    <nav className={`${theme.primary} shadow-lg`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link
            href="/"
            className={`${theme.text} text-lg font-semibold hover:opacity-80 transition-opacity`}
          >
            {theme.ngoName}
          </Link>
          <div className="hidden md:block">
            <div className="ml-10 flex items-baseline space-x-4">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  style={{
                    backgroundColor:
                      pathname === item.href ? theme.primary : 'transparent',
                    color:
                      pathname === item.href ? theme.text : theme.textLight,
                  }}
                  className={`px-3 py-2 rounded-md text-sm font-medium hover:bg-opacity-20 hover:text-${theme.text}`}
                >
                  {item.label}
                </Link>
              ))}
              <button
                onClick={logout}
                className={`px-3 py-2 rounded-md text-sm font-medium hover:bg-opacity-20 hover:text-${theme.text}`}
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};
