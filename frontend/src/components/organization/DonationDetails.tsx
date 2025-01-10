'use client'

import { Card, CardHeader, CardTitle, CardContent } from "./ui/card"
import { Donation } from "@/app/actions/donation"

interface DonationDetailsProps {
  donation: Donation
}

export default function DonationDetails({ donation }: DonationDetailsProps) {
  return (
    <Card className="mb-4">
      <CardHeader>
        <CardTitle className="flex justify-between items-center">
          <span>Donation {donation.donationId}</span>
          <span className="text-lg font-normal">{donation.amount.amount}</span>
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
              <p className="font-medium">{new Date(donation.date).toLocaleDateString()}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Earmarking</p>
              <p className="font-medium">{donation.earmarking}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Current Status</p>
              <p className="font-medium">{donation.status[donation.status.length - 1]?.status || 'Unknown'}</p>
            </div>
          </div>

          <div>
            <h3 className="text-lg font-semibold mb-2">Status History</h3>
            <div className="space-y-3">
              {donation.status.map((status, index) => (
                <div 
                  key={index} 
                  className="p-3 bg-secondary rounded-lg"
                >
                  <div className="flex justify-between items-start mb-1">
                    <span className="font-medium">{status.status}</span>
                    <span className="text-sm text-muted-foreground">
                      {new Date(status.date).toLocaleString()}
                    </span>
                  </div>
                  {status.description && (
                    <p className="text-sm text-muted-foreground">{status.description}</p>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
} 