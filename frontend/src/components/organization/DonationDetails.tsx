'use client';

import { DonationWithDonor } from '@/types/types';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Button } from './ui/button';
import { useTheme } from '@/contexts/ThemeContext';
import { useState } from 'react';
import axiosInstance from '@/app/organization/api/axiosInstance';
import { Textarea } from './ui/textarea';
import { toast, ToastContainer } from 'react-toastify';
import axios from 'axios';

type NotificationModalState = {
  donation: DonationWithDonor;
  message: string;
};

interface DonationDetailsProps {
  donation: DonationWithDonor;
}

export default function DonationDetails({ donation }: DonationDetailsProps) {
  const { theme } = useTheme();

  const [notificationModalState, setNotificationModalState] =
    useState<NotificationModalState>();
  const [isSendingNotification, setIsSendingNotification] = useState(false);

  const sendNotification = async (modalState: NotificationModalState) => {
    const { donation, message } = modalState;

    setIsSendingNotification(true);
    try {
      await axiosInstance.post(
        `/notify/${donation.organisation}/${donation.donationId}`,
        {
          message: message,
        },
      );
      setNotificationModalState(undefined);
      toast.success('Sent notification successfully.', {
        position: 'bottom-right',
      });
    } catch (error) {
      let errorMessage = 'Failed to create earmarking';

      if (axios.isAxiosError(error)) {
        errorMessage =
          error.response?.data?.error || error.message || errorMessage;
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }

      console.error(errorMessage);
      toast.error('An error occured while sending the notification.', {
        position: 'bottom-right',
      });
    } finally {
      setIsSendingNotification(false);
    }
  };

  console.log(donation);

  return (
    <>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle className="flex justify-between items-center">
            <span>Donation {donation.donationId}</span>
            <span className="text-lg font-normal">
              {donation.amount.amount}€
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Organization</p>
                <p className="font-medium">{donation.organisation}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Date</p>
                <p className="font-medium">
                  {new Date(donation.date).toLocaleDateString()}
                </p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Donor Name</p>
                <p className="font-medium">{donation.donor.name}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Donor Email</p>
                <p className="font-medium">{donation.donor.email}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Earmarking</p>
                <p className="font-medium">{donation.earmarking}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Current Status</p>
                <p className="font-medium">
                  {donation.status[donation.status.length - 1]?.status ||
                    'Unknown'}
                </p>
              </div>
            </div>

            <div>
              <h3 className="text-lg font-semibold mb-2">Status History</h3>
              <div className="space-y-3">
                {donation.status.map((status, index) => (
                  <div key={index} className="p-3 bg-secondary rounded-lg">
                    <div className="flex justify-between items-start mb-1">
                      <span className="font-medium">{status.status}</span>
                      <span className="text-sm text-muted-foreground">
                        {new Date(status.date).toLocaleString()}
                      </span>
                    </div>
                    {status.description && (
                      <p className="text-sm text-muted-foreground">
                        {status.description}
                      </p>
                    )}
                  </div>
                ))}
              </div>
              <Button
                onClick={() =>
                  setNotificationModalState({
                    donation: donation,
                    message: theme.emailTemplates.manualTemplate,
                  })
                }
                className={`${theme.primary} mt-3 w-full`}
              >
                Send notification
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {notificationModalState && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
            <Textarea
              value={notificationModalState.message}
              onChange={(e) =>
                setNotificationModalState((prev) =>
                  prev ? { ...prev, message: e.target.value } : prev,
                )
              }
              placeholder="Description"
              className="min-h-[200px] resize-y mb-3"
            />
            <div className="flex justify-end space-x-4">
              <button
                onClick={() => setNotificationModalState(undefined)}
                className="bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded shadow text-sm"
                disabled={isSendingNotification}
              >
                Cancel
              </button>
              <button
                onClick={() => sendNotification(notificationModalState)}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded shadow text-sm"
                disabled={isSendingNotification}
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      <ToastContainer />
    </>
  );
}
