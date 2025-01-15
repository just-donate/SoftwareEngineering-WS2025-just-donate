'use client';

import { EarmarkingImage } from '@/types/types';
import axiosInstance from '../api/axiosInstance';

export async function fetchEarmarkingImages(
  orgId: string,
  earmarking: string,
): Promise<EarmarkingImage[]> {
  try {
    const response = await axiosInstance.get(
      `/public/organisation/${orgId}/earmarking/${earmarking}/image/list`,
    );

    console.log(response.data);
    return response.data; // Return the array of file URLs
  } catch (error) {
    console.error('Failed to fetch earmarking images:', error);
    return [];
  }
}

export async function uploadEarmarkingImage(
  orgId: string,
  earmarking: string,
  image: File,
) {
  try {
    const base64ImageString = await readFileAsDataURL(image);
    const response = await axiosInstance.post(
      `/organisation/${orgId}/earmarking/${earmarking}/image`,
      {
        fileUrl: base64ImageString,
      },
    );

    if (response.status !== 200) {
      throw new Error('Failed to upload image');
    }

    return { success: true };
  } catch (error) {
    console.error('Failed to upload image:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Failed to upload image',
    };
  }
}

function readFileAsDataURL(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      resolve(reader.result as string);
    };
    reader.onerror = () => {
      reject(new Error('Failed to read file'));
    };
    reader.readAsDataURL(file);
  });
}
