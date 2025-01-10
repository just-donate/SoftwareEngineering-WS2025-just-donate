'use client'

import { useState } from 'react'
import { Input } from "./ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select"
import DonationDetails from "./DonationDetails"
import { Donation } from "@/app/actions/donation"

interface DonationsListProps {
  initialDonations: Donation[]
}

export default function DonationsList({ initialDonations }: DonationsListProps) {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')

  // Get unique status values from all donations
  const uniqueStatuses = Array.from(new Set(
    initialDonations.flatMap(d => d.status.map(s => s.status))
  ))

  const filteredDonations = initialDonations.filter(donation => {
    const matchesSearch = 
      donation.donationId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      donation.organisation.toLowerCase().includes(searchTerm.toLowerCase()) ||
      donation.earmarking.toLowerCase().includes(searchTerm.toLowerCase())

    const matchesStatus = 
      statusFilter === 'all' || 
      donation.status[donation.status.length - 1]?.status === statusFilter

    return matchesSearch && matchesStatus
  })

  return (
    <div className="space-y-4">
      <div className="flex gap-4">
        <Input
          placeholder="Search donations..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="max-w-sm"
        />
        <Select 
          value={statusFilter} 
          onValueChange={setStatusFilter}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Statuses</SelectItem>
            {uniqueStatuses.map(status => (
              <SelectItem key={status} value={status}>
                {status}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="space-y-4">
        {filteredDonations.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">
            No donations found matching your criteria
          </p>
        ) : (
          filteredDonations.map(donation => (
            <DonationDetails 
              key={donation.donationId} 
              donation={donation}
            />
          ))
        )}
      </div>
    </div>
  )
} 