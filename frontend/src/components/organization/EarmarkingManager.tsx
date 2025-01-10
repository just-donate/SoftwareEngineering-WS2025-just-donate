'use client'

import { useState } from 'react'
import { Button } from "./ui/button"
import { Input } from "./ui/input"
import { Card, CardHeader, CardTitle, CardContent } from "./ui/card"
import { Earmarking } from '@/types/types'
import { createEarmarking } from '@/app/actions/earmarking'

interface EarmarkingManagerProps {
  initialEarmarkings: Earmarking[]
  organizationId: string
}

export default function EarmarkingManager({ initialEarmarkings, organizationId }: EarmarkingManagerProps) {
  const [earmarkings, setEarmarkings] = useState<Earmarking[]>(initialEarmarkings)
  const [newEarmarkingName, setNewEarmarkingName] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  const addEarmarking = async () => {
    if (!newEarmarkingName) return

    const result = await createEarmarking(organizationId, newEarmarkingName)
    
    if (result.success) {
      // Optimistically update the UI
      const newEarmarking: Earmarking = {
        name: newEarmarkingName
      }
      setEarmarkings([...earmarkings, newEarmarking])
      setNewEarmarkingName('')
      setSuccessMessage('Earmarking created successfully!')
      setError('')
      setTimeout(() => setSuccessMessage(''), 3000)
    } else {
      setError(result.error || 'Failed to create earmarking')
      setSuccessMessage('')
    }
  }

  return (
    <div>
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Add New Earmarking</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex space-x-2">
              <Input
                value={newEarmarkingName}
                onChange={(e) => setNewEarmarkingName(e.target.value)}
                placeholder="Earmarking name"
              />
              <Button onClick={addEarmarking}>Add</Button>
            </div>
            {error && <div className="text-red-500">{error}</div>}
            {successMessage && <div className="text-green-500">{successMessage}</div>}
          </div>
        </CardContent>
      </Card>
      
      <Card>
        <CardHeader>
          <CardTitle>Existing Earmarkings</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {earmarkings.map((earmarking) => (
              <li key={earmarking.name} className="p-2 bg-secondary rounded-lg">
                {earmarking.name}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  )
} 