import React from 'react';
import {
  render,
  screen,
  fireEvent,
  waitFor,
  act,
} from '@testing-library/react';
import GalleryManager from '../../src/components/organization/GalleryManager';
import {
  fetchEarmarkingImages,
  uploadEarmarkingImage,
} from '../../src/app/organization/gallery/gallery';
import { fetchEarmarkings } from '../../src/app/organization/earmarkings/earmarkings';
import { Earmarking, EarmarkingImage } from '../../src/types/types';
import '@testing-library/jest-dom';

// Mock the fetchEarmarkingImages and uploadEarmarkingImage functions
jest.mock('../../src/app/organization/gallery/gallery', () => ({
  fetchEarmarkingImages: jest.fn(),
  uploadEarmarkingImage: jest.fn(),
}));

jest.mock('../../src/app/organization/earmarkings/earmarkings', () => ({
  fetchEarmarkings: jest.fn(),
}));

const mockEarmarkings: Earmarking[] = [
  { name: 'Test Earmarking', description: 'Test Description' },
];

describe('GalleryManager', () => {
  const organisationId = '591671920';

  beforeEach(() => {
    jest.clearAllMocks(); // Clear mocks before each test
    (fetchEarmarkings as jest.Mock).mockResolvedValue(mockEarmarkings);
  });

  it('renders the component and fetches earmarking images', async () => {
    const mockImages: EarmarkingImage[] = [
      { image: { fileUrl: 'https://example.com/image1.jpg' } },
    ];

    (fetchEarmarkingImages as jest.Mock).mockResolvedValue(mockImages);

    await act(async () => {
      render(<GalleryManager organisationId={organisationId} />);
    });

    await waitFor(() => {
      fireEvent.click(screen.getByTestId('select-earmarking-view'));
      fireEvent.click(screen.getByText(/Test Earmarking/i));
    });

    // Wait for the images to be fetched and rendered
    await waitFor(() => {
      expect(fetchEarmarkingImages).toHaveBeenCalledWith(
        organisationId,
        expect.any(String),
      ); // Check if the function was called
      expect(
        screen.getByAltText('Photo for Test Earmarking'),
      ).toBeInTheDocument(); // Check if the first image is rendered
    });
  });

  it('handles file upload successfully', async () => {
    (uploadEarmarkingImage as jest.Mock).mockResolvedValue({ success: true });

    await act(async () => {
      render(<GalleryManager organisationId={organisationId} />);
    });

    const file = new File(['dummy content'], 'example.png', {
      type: 'image/png',
    });

    // Simulate file selection
    await waitFor(() => {
      const fileInput = screen.getByTestId('photo-upload');
      fireEvent.change(fileInput, { target: { files: [file] } });

      // Simulate selecting an earmarking by clicking the first select earmarking and then the option
      const select = screen.getByTestId('select-earmarking-upload');
      fireEvent.click(select);
      const option = screen.getByText(/Test Earmarking/i);
      fireEvent.click(option);
    });

    // Simulate clicking the upload button
    await waitFor(() => {
      const uploadButton = screen.getByTestId('upload-button');
      fireEvent.click(uploadButton);
    });

    // Wait for the upload to complete
    await waitFor(() => {
      expect(uploadEarmarkingImage).toHaveBeenCalledWith(
        organisationId,
        'Test Earmarking',
        file,
      );
      expect(
        screen.getByText(/photo uploaded successfully/i),
      ).toBeInTheDocument(); // Check success message
    });
  });

  it('displays an error message on upload failure', async () => {
    (uploadEarmarkingImage as jest.Mock).mockResolvedValue({
      success: false,
      error: 'Upload failed',
    });

    await act(async () => {
      render(<GalleryManager organisationId={organisationId} />);
    });

    const file = new File(['dummy content'], 'example.png', {
      type: 'image/png',
    });

    // Simulate file selection
    await waitFor(() => {
      const fileInput = screen.getByTestId('photo-upload');
      fireEvent.change(fileInput, { target: { files: [file] } });
    });

    // Simulate selecting an earmarking by clicking the select and then the option
    await waitFor(() => {
      fireEvent.click(screen.getByTestId('select-earmarking-upload'));
      fireEvent.click(screen.getByText(/Test Earmarking/i));
    });

    // Simulate clicking the upload button
    await waitFor(() => {
      const uploadButton = screen.getByTestId('upload-button');
      fireEvent.click(uploadButton);
    });

    // Wait for the error message to be displayed
    await waitFor(() => {
      expect(screen.getByText(/upload failed/i)).toBeInTheDocument(); // Check error message
    });
  });
});
