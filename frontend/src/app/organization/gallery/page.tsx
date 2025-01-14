'use client';

import GalleryManager from '../../../components/organization/GalleryManager';
import withAuth from '../api/RequiresAuth';
import { useTheme } from '@/contexts/ThemeContext';

function GalleryPage() {
  const organisationId = '591671920';

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Gallery</h1>
      <GalleryManager organisationId={organisationId} />
    </div>
  );
}

export default withAuth(GalleryPage);
