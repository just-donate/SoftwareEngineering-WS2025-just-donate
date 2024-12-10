'use client'

import { useState } from 'react'
import { Donation } from '@/types'
import { Button } from "@/components/ui/button"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Textarea } from "@/components/ui/textarea"

// Mock data for demonstration
const mockDonations: Donation[] = [
  { id: '1', donorEmail: 'donor1@example.com', amount: 100, status: 'received', lastUpdated: '2023-06-01' },
  { id: '2', donorEmail: 'donor2@example.com', amount: 200, status: 'transferred', lastUpdated: '2023-06-02' },
  { id: '3', donorEmail: 'donor3@example.com', amount: 300, status: 'in use', lastUpdated: '2023-06-03' },
  { id: '4', donorEmail: 'donor4@example.com', amount: 400, status: 'used', lastUpdated: '2023-06-04' },
]

export default function DonationsPage() {
  const [donations] = useState<Donation[]>(mockDonations)
  const [selectedDonation, setSelectedDonation] = useState<Donation | null>(null)
  const [updateContent, setUpdateContent] = useState('')

  const handleSendUpdate = () => {
    // Here you would typically send the update to the backend
    console.log(`Sending update to ${selectedDonation?.donorEmail}:`, updateContent)
    // After sending, you might want to update the lastUpdated field
    // This is just a mock update
    setSelectedDonation(null)
    setUpdateContent('')
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Donations</h1>
      <Card>
        <CardHeader>
          <CardTitle>Donation List</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Donor Email</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Last Updated</TableHead>
                <TableHead>Action</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {donations.map((donation) => (
                <TableRow key={donation.id}>
                  <TableCell>{donation.donorEmail}</TableCell>
                  <TableCell>${donation.amount}</TableCell>
                  <TableCell>
                    <Badge variant={
                      donation.status === 'received' ? 'default' :
                      donation.status === 'transferred' ? 'secondary' :
                      donation.status === 'in use' ? 'info' :
                      'success'
                    }>
                      {donation.status}
                    </Badge>
                  </TableCell>
                  <TableCell>{donation.lastUpdated}</TableCell>
                  <TableCell>
                    <Dialog>
                      <DialogTrigger asChild>
                        <Button variant="outline" onClick={() => setSelectedDonation(donation)}>
                          Send Update
                        </Button>
                      </DialogTrigger>
                      <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                          <DialogTitle>Send Update to {selectedDonation?.donorEmail}</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                          <Textarea
                            placeholder="Type your update here..."
                            value={updateContent}
                            onChange={(e) => setUpdateContent(e.target.value)}
                          />
                          <Button onClick={handleSendUpdate}>Send Update</Button>
                        </div>
                      </DialogContent>
                    </Dialog>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}

