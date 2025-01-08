'use client'

import { Theme } from '@/styles/themes';
import { DonationList } from './DonationList';
import { useTheme } from '@/contexts/ThemeContext';
import { Donation } from '@/types/types';

interface TrackingPageClientProps {
    donations: Donation[] | null;
    trackingId: string;
}

export function TrackingPageClient({ donations, trackingId }: TrackingPageClientProps) {
    const { theme } = useTheme();
    
    return (
        <div className={`min-h-screen ${theme.background}`}>
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
