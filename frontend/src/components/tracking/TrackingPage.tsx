'use client'

import { DonationList } from './DonationList';
import { useTheme } from '@/contexts/ThemeContext';
import { Donation } from '@/types/types';
import { Navigation } from '@/components/tracking/Navigation';
import { Theme } from '@/styles/themes';

interface TrackingPageClientProps {
    donations: Donation[] | null;
    theme: Theme;
}

export function TrackingPageClient({ donations, theme }: TrackingPageClientProps) {

    return (
        <div className={`min-h-screen ${theme.background}`}>
            <Navigation links={[{ link: '/help', name: 'Help' }]} />
            <main className={`${theme.text}`}>
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    {donations ? (
                        <DonationList donations={donations} />
                    ) : (
                        <div className="text-center py-12">
                            <h2 className="text-2xl font-semibold">No donations found</h2>
                            <p className="mt-2">The tracking ID you provided could not be found.</p>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
}
