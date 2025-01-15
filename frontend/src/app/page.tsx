'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useTheme } from '@/contexts/ThemeContext';
import { Navigation } from '@/components/tracking/Navigation';

export default function SearchPage() {
  const { theme } = useTheme();
  const router = useRouter();
  const [searchInput, setSearchInput] = useState('');
  const [email, setEmail] = useState('');
  const [isEmailSubmitted, setIsEmailSubmitted] = useState(false);
  const [dropdownVisible, setDropdownVisible] = useState(false);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (
      searchInput.startsWith('http://') ||
      searchInput.startsWith('https://')
    ) {
      window.location.href = searchInput;
    } else {
      router.push(`/tracking?id=${searchInput}`);
    }
  };

  const handleForgotTrackingLink = () => {
    setDropdownVisible(!dropdownVisible);
  };

  const handleEmailSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log(`Email submitted: ${email}`);
    setIsEmailSubmitted(true);
  };

  return (
    <div
      className={`h-screen ${theme.background}`}
      style={{
        backgroundImage:
          'url(https://lirp.cdn-website.com/58002456/dms3rep/multi/opt/IMG_6410-358b77c4-976cb433-1920w.jpg)',
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}
    >
      <Navigation
        links={[
          { link: '/organization/', name: 'Organization Login' },
          { link: '/help', name: 'Help' },
        ]}
      />
      <main
        className={`${theme.text} max-w-4xl mx-auto px-4 flex flex-col items-center justify-center h-[calc(100%-4rem)]`}
      >
        <div
          className={`${theme.card} p-6 rounded-lg shadow-lg w-full max-w-2xl mb-6`}
        >
          <h2 className={`text-2xl font-bold mb-4 ${theme.text}`}>
            Welcome to JustDonate
          </h2>
          <p className={`mb-4 ${theme.text}`}>
            JustDonate helps you track your donated money to an organization
            step by step and lets you see exactly what it is used for. By
            entering a tracking ID, you can directly access your dashboard. You
            should have received the ID via mail after donating to an
            organization using JustDonate.
          </p>
        </div>
        <div
          className={`${theme.card} p-6 rounded-lg shadow-lg w-full max-w-2xl`}
        >
          <form
            onSubmit={handleSearch}
            className="flex items-center space-x-4 w-full"
          >
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Enter your tracking link or ID"
              className={`flex-grow p-2 border rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 ${theme.text} ${theme.background}`}
              style={{ flex: '2 1 0%' }}
            />
            <button
              type="submit"
              className={`${theme.primary} px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap`}
              style={{ flex: '1 1 0%' }}
            >
              Search
            </button>
          </form>
          <div
            className={`relative mt-4 w-full transition-all duration-200 ${dropdownVisible ? 'h-auto' : 'h-12'}`}
          >
            <button
              type="button"
              onClick={handleForgotTrackingLink}
              className={`px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap w-full text-left ${theme.secondary} border ${theme.text}`}
            >
              Forgot Tracking Link?
            </button>
            {dropdownVisible && (
              <div
                className={`mt-2 p-4 border rounded-md shadow-lg ${theme.card} z-10`}
              >
                <form
                  onSubmit={handleEmailSubmit}
                  className="flex items-center space-x-4 w-full"
                >
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email"
                    className={`flex-grow p-2 border rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 ${theme.text} ${theme.background}`}
                    style={{ flex: '2 1 0%' }}
                  />
                  <button
                    type="submit"
                    className={`${theme.primary} px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap`}
                    style={{ flex: '1 1 0%' }}
                  >
                    Submit
                  </button>
                </form>
              </div>
            )}
          </div>
          {isEmailSubmitted && (
            <p className={`${theme.textLight} mt-4`}>
              Thank you! A link has been sent to your email.
            </p>
          )}
        </div>
      </main>
    </div>
  );
}
