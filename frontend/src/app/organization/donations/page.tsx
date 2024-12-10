'use client'

import { useState } from 'react'
import { Donation } from '../../../types/types'
import { Button } from "../../../components/organization/ui/button"
import { Card, CardHeader, CardTitle, CardContent } from "../../../components/organization/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../../../components/organization/ui/table"
import { Badge } from "../../../components/organization/ui/badge"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../../components/organization/ui/dialog"
import { Textarea } from "../../../components/organization/ui/textarea"
import { mockDonations } from '@/app/tracking/[id]/donations'

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
              {donations.map((donation) => {
                const latestStatus = donation.status[donation.status.length - 1];
                return (
                  <TableRow key={donation.id}>
                    <TableCell>{donation.donorEmail}</TableCell>
                  <TableCell>${donation.amount}</TableCell>
                  <TableCell>
                    <Badge>
                      {latestStatus.status}
                    </Badge>
                  </TableCell>
                  <TableCell>{latestStatus.date}</TableCell>
                  <TableCell>
                    <Dialog>
                      <DialogTrigger asChild>
                        <Button onClick={() => setSelectedDonation(donation)}>
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
                )
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}

