'use client'

import { useState } from 'react'
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { Earmarking } from '@/types'

export default function EarmarkingsPage() {
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>([])
  const [newEarmarkingName, setNewEarmarkingName] = useState('')

  const addEarmarking = () => {
    if (newEarmarkingName) {
      const newEarmarking: Earmarking = {
        id: Date.now().toString(),
        name: newEarmarkingName,
        organizationId: '1', // This should be the actual organization ID
      }
      setEarmarkings([...earmarkings, newEarmarking])
      setNewEarmarkingName('')
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Manage Earmarkings</h1>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Add New Earmarking</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex space-x-2">
            <Input
              value={newEarmarkingName}
              onChange={(e) => setNewEarmarkingName(e.target.value)}
              placeholder="Earmarking name"
            />
            <Button onClick={addEarmarking}>Add</Button>
          </div>
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle>Existing Earmarkings</CardTitle>
        </CardHeader>
        <CardContent>
          <ul>
            {earmarkings.map((earmarking) => (
              <li key={earmarking.id}>{earmarking.name}</li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
}

