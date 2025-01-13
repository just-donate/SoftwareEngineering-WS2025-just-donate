import {
  fetchDonations,
  createDonation,
} from '../../src/app/organization/donations/donations';
import axios from 'axios';
import axiosInstance from '../../src/app/organization/api/axiosInstance';

// Mock the axiosInstance
jest.mock('../../src/app/organization/api/axiosInstance');

describe('Donations API', () => {
  afterEach(() => {
    jest.clearAllMocks(); // Clear mocks after each test
  });

  describe('fetchDonations', () => {
    it('fetches donations successfully', async () => {
      const mockOrgId = '591671920';
      const mockResponse = {
        data: {
          donations: [
            {
              donationId: '1',
              donorEmail: 'john@example.com',
              amount: { amount: '100.0' },
              earmarking: 'General Purpose',
              organisation: 'Organization 1',
              date: '2024-01-01',
              status: [
                {
                  status: 'Completed',
                  date: '2024-01-01',
                  description: 'Donation completed successfully',
                },
              ],
            },
          ],
        },
      };

      (axiosInstance.get as jest.Mock).mockResolvedValue(mockResponse);

      const donations = await fetchDonations(mockOrgId);

      expect(donations).toEqual(mockResponse.data.donations);
      expect(axiosInstance.get).toHaveBeenCalledWith(
        `/donate/${mockOrgId}/donations`,
      );
    });

    it('handles errors when fetching donations', async () => {
      const mockOrgId = '591671920';
      const mockError = new Error('Network Error');

      (axiosInstance.get as jest.Mock).mockRejectedValue(mockError);

      const donations = await fetchDonations(mockOrgId);

      expect(donations).toEqual([]); // Should return an empty array on error
      expect(axiosInstance.get).toHaveBeenCalledWith(
        `/donate/${mockOrgId}/donations`,
      );
    });
  });

  describe('createDonation', () => {
    it('creates a donation successfully', async () => {
      const mockOrgId = '591671920';
      const mockDonationData = {
        donorName: 'John Doe',
        donorEmail: 'john@example.com',
        amount: { amount: '100.0' },
        earmarking: 'General Purpose',
        accountName: 'Main Account',
      };

      const exprectedSentData = {
        donorName: 'John Doe',
        donorEmail: 'john@example.com',
        amount: { amount: '100.0' },
        earmarking: 'General Purpose',
      };

      const mockResponse = {
        status: 200,
      };

      (axiosInstance.post as jest.Mock).mockResolvedValue(mockResponse);

      const result = await createDonation(
        mockOrgId,
        mockDonationData.donorName,
        mockDonationData.donorEmail,
        mockDonationData.amount,
        mockDonationData.earmarking,
        mockDonationData.accountName,
      );

      expect(result).toEqual({ success: true });
      expect(axiosInstance.post).toHaveBeenCalledWith(
        `/donate/${mockOrgId}/account/${mockDonationData.accountName}`,
        exprectedSentData,
      );
    });

    it('handles errors when creating a donation', async () => {
      const mockOrgId = '591671920';
      const mockDonationData = {
        donorName: 'John Doe',
        donorEmail: 'john@example.com',
        amount: { amount: '100.0' },
        earmarking: 'General Purpose',
        accountName: 'Main Account',
      };

      const mockError = new Error('Failed to create donation');

      (axiosInstance.post as jest.Mock).mockRejectedValue(mockError);

      const result = await createDonation(
        mockOrgId,
        mockDonationData.donorName,
        mockDonationData.donorEmail,
        mockDonationData.amount,
        mockDonationData.earmarking,
        mockDonationData.accountName,
      );

      expect(result).toEqual({
        success: false,
        error: 'Failed to create donation',
      });
    });
  });

  it('fails if status code is not 200', async () => {
    const mockOrgId = '591671920';
    const mockDonationData = {
      donorName: 'John Doe',
      donorEmail: 'john@example.com',
      amount: { amount: '100.0' },
      earmarking: 'General Purpose',
      accountName: 'Main Account',
    };

    const mockResponse = {
      status: 404,
    };

    (axiosInstance.post as jest.Mock).mockResolvedValue(mockResponse);

    const result = await createDonation(
      mockOrgId,
      mockDonationData.donorName,
      mockDonationData.donorEmail,
      mockDonationData.amount,
      mockDonationData.earmarking,
      mockDonationData.accountName,
    );

    expect(result).toEqual({
      success: false,
      error: 'Failed to create donation',
    });
  });
});
