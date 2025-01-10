import { getEarmarkings } from '@/app/actions/earmarking';
import EarmarkingManager from '@/components/organization/EarmarkingManager';

export default async function EarmarkingsPage() {
  // TODO: Get the organization ID from the session/context
  const organizationId = '591671920';
  const earmarkings = await getEarmarkings(organizationId);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Earmarkings</h1>
      <EarmarkingManager
        initialEarmarkings={earmarkings}
        organizationId={organizationId}
      />
    </div>
  );
}
