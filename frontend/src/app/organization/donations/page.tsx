import { getDonations } from '@/app/actions/donation'
import DonationsList from '@/components/organization/DonationsList'

export default async function DonationsPage() {

  const organizationId = '591671920'
  const donations = await getDonations(organizationId)

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Donations</h1>
      <DonationsList initialDonations={donations} />
    </div>
  )
}

