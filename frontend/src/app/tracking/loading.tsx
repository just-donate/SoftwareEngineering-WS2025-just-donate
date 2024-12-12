'use client'

import { useTheme } from '../../contexts/ThemeContext';
import { Navigation } from '@/components/tracking/Navigation';

export default function TrackingLoading() {

  const { theme } = useTheme();

  return (
    <div className={`min-h-screen ${theme.background}`}>
      <Navigation links={[{ link: '/help', name: 'Help' }]} />
      <main className={`${theme.text}`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          Loading donations...
        </div>
      </main>
    </div>
  );
}


