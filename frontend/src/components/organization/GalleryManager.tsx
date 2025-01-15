'use client';

import { Earmarking, EarmarkingImage } from '@/types/types';
import { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from './ui/select';
import { Input } from './ui/input';
import { Button } from './ui/button';
import Image from 'next/image';
import { fetchEarmarkings } from '@/app/organization/earmarkings/earmarkings';
import {
  fetchEarmarkingImages,
  uploadEarmarkingImage,
} from '@/app/organization/gallery/gallery';

interface GalleryManagerProps {
  organisationId: string;
}

export default function GalleryManager({
  organisationId,
}: GalleryManagerProps) {
  const [earmarkingImages, setEarmarkingImages] = useState<EarmarkingImage[]>(
    [],
  );
  const [selectedEarmarking, setSelectedEarmarking] = useState<
    string | undefined
  >(undefined);
  const [earmarkingToUpload, setEarmarkingToUpload] = useState<
    string | undefined
  >(undefined);
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    fetchEarmarkings(organisationId).then((earmarkings) =>
      setEarmarkings(earmarkings),
    );
  }, [organisationId]);

  const filteredPhotos = async (selectedEarmarking: string | undefined) => {
    if (!selectedEarmarking) return;
    const images = await fetchEarmarkingImages(
      organisationId,
      selectedEarmarking,
    );

    setEarmarkingImages(images);
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setFile(file);
    }
  };

  const handleUpload = async () => {
    if (!file || !earmarkingToUpload) {
      setError('Please fill in all fields');
      return;
    }

    const result = await uploadEarmarkingImage(
      organisationId,
      earmarkingToUpload,
      file,
    );
    if (result.success) {
      if (selectedEarmarking === earmarkingToUpload) {
        filteredPhotos(selectedEarmarking);
      }
      setSuccess('Photo uploaded successfully');
      setError(null);
    } else {
      setError(result.error || 'Failed to upload image');
      setSuccess(null);
    }
  };

  const updateSelectedEarmarking = (earmarking: string) => {
    setSelectedEarmarking(earmarking);
    filteredPhotos(earmarking);
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Photo Gallery</h1>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Upload Photo</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4">
            <Select
              value={earmarkingToUpload}
              onValueChange={setEarmarkingToUpload}
            >
              <SelectTrigger data-testid="select-earmarking-upload">
                <SelectValue placeholder="Select Earmarking to upload to" />
              </SelectTrigger>
              <SelectContent>
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
              data-testid="photo-upload"
            />
            <Button
              onClick={handleUpload}
              disabled={!earmarkingToUpload || !file}
              data-testid="upload-button"
            >
              Upload
            </Button>
            {error && <p className="text-red-500">{error}</p>}
            {success && <p className="text-green-500">{success}</p>}
          </div>
        </CardContent>
      </Card>
      <Select
        value={selectedEarmarking}
        onValueChange={updateSelectedEarmarking}
      >
        <SelectTrigger data-testid="select-earmarking-view">
          <SelectValue placeholder="Select Earmarking" />
        </SelectTrigger>
        <SelectContent>
          {earmarkings.map((earmarking) => (
            <SelectItem key={earmarking.name} value={earmarking.name}>
              {earmarking.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Card className="mt-4">
        <CardHeader>
          <CardTitle>Photos</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {earmarkingImages.map((image, index) => (
              <div key={index} className="relative aspect-square">
                <Image
                  src={image.image.fileUrl}
                  alt={`Photo for ${selectedEarmarking}`}
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
