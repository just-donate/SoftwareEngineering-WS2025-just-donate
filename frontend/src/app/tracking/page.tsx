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

  return (
    <TrackingPageClient donations={donations} trackingId={id} />
  );
}


