'use server';

import { themes } from '@/styles/themes';
import { getTheme } from '../actions/theme';
import { getDonations } from './donations';
import { TrackingPageClient } from '@/components/tracking/TrackingPage';

export default async function Tracking({
  searchParams,
}: {
  searchParams: { id: string };
}) {
  const { id } = searchParams;
  const donations = await getDonations(id);
  const theme = (await getTheme()) || themes.default;

  if (!donations) {
    return <div>No donations found</div>;
  }

  return <TrackingPageClient donations={donations.donations} theme={theme} />;
}
