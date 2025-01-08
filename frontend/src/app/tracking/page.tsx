'use server'

import { getDonations } from './donations';
import { TrackingPageClient } from '@/components/tracking/TrackingPage';



export default async function Tracking({
  searchParams,
}: {
  searchParams: { id: string };
}) {
  const { id } = searchParams;
  const donations = await getDonations(id);

  if (!donations) {
    return <div>No donations found</div>;
  }

  return (
    <TrackingPageClient donations={donations.donations} trackingId={id} />
  );
}


