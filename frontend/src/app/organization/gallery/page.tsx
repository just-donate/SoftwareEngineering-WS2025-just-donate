'use client';

import { useState } from 'react';
import { Earmarking, Photo } from '../../../types/types';
import { Button } from '../../../components/organization/ui/button';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '../../../components/organization/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../../components/organization/ui/select';
import { Input } from '../../../components/organization/ui/input';
import Image from 'next/image';
import withAuth from '../api/RequiresAuth';
import { useTheme } from '@/contexts/ThemeContext';

// Mock data for demonstration
const mockEarmarkings: Earmarking[] = [
  { name: 'Project A', description: 'Description for Project A' },
  { name: 'Project B', description: 'Description for Project B' },
];

const mockPhotos: Photo[] = [
  {
    id: '1',
    url: 'https://placeholder.pics/svg/200',
    earmarkingId: '1',
    uploadDate: '2023-06-01',
  },
  {
    id: '2',
    url: 'https://placeholder.pics/svg/200',
    earmarkingId: '1',
    uploadDate: '2023-06-02',
  },
  {
    id: '3',
    url: 'https://placeholder.pics/svg/200',
    earmarkingId: '2',
    uploadDate: '2023-06-03',
  },
];

function GalleryPage() {
  const { theme } = useTheme();
  const [earmarkings] = useState<Earmarking[]>(mockEarmarkings);
  const [photos, setPhotos] = useState<Photo[]>(mockPhotos);
  const [selectedEarmarking, setSelectedEarmarking] = useState<string>('');
  const [file, setFile] = useState<File | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFile(e.target.files[0]);
    }
  };

  const handleUpload = () => {
    if (file && selectedEarmarking) {
      // In a real application, you would upload the file to a server here
      // For this example, we'll just add a new photo to the state
      const newPhoto: Photo = {
        id: Date.now().toString(),
        url: URL.createObjectURL(file),
        earmarkingId: selectedEarmarking,
        uploadDate: new Date().toISOString().split('T')[0],
      };
      setPhotos([...photos, newPhoto]);
      setFile(null);
    }
  };

  const filteredPhotos = selectedEarmarking
    ? photos.filter((photo) => photo.earmarkingId === selectedEarmarking)
    : photos;

  return (
    <div className={'container mx-auto p-4'}>
      <h1 className={`text-2xl font-bold mb-4 ${theme.text}`}>Photo Gallery</h1>
      <Card className={theme.card}>
        <CardHeader>
          <CardTitle className={theme.text}>Upload Photo</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4">
            <Select onValueChange={setSelectedEarmarking}>
              <SelectTrigger className={`${theme.background} ${theme.text}`}>
                <SelectValue placeholder="Select Earmarking" />
              </SelectTrigger>
              <SelectContent className={theme.card}>
                {earmarkings.map((earmarking) => (
                  <SelectItem key={earmarking.name} value={earmarking.name}>
                    {earmarking.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Input
              type="file"
              onChange={handleFileChange}
              accept="image/*"
              className={`${theme.background} ${theme.text}`}
            />
            <Button
              onClick={handleUpload}
              disabled={!file || !selectedEarmarking}
              className={theme.primary}
            >
              Upload Photo
            </Button>
          </div>
        </CardContent>
      </Card>
      <Card className="${theme.card} mt-4">
        <CardHeader>
          <CardTitle className={theme.text}>Photos</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredPhotos.map((photo) => (
              <div key={photo.id} className="relative aspect-square">
                <Image
                  src={photo.url}
                  alt={`Photo for ${earmarkings.find((e) => e.name === photo.earmarkingId)?.name}`}
                  fill
                  className="object-cover rounded-md"
                />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

export default withAuth(GalleryPage);
