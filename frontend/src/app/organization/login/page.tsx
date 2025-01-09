'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useTheme } from '@/contexts/ThemeContext'
import { Button } from "../../../components/organization/ui/button"
import { Input } from "../../../components/organization/ui/input"
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "../../../components/organization/ui/card"

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const router = useRouter()
  const { theme } = useTheme()

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault()
    // Here you would typically validate the credentials against your backend
    // For this example, we'll just redirect to the dashboard
    router.push('/dashboard')
  }

  return (
    <div className={`flex items-center justify-center min-h-screen ${theme.background}`}>
      <div className={`${theme.card} p-8 rounded-lg shadow-lg w-full max-w-md`}>
        <h1 className={`text-2xl font-bold mb-6 ${theme.text}`}>Organization Login</h1>
        <form className="space-y-4">
          <div>
            <label htmlFor="email" className={`block mb-1 ${theme.text}`}>Email</label>
            <input
              type="email"
              id="email"
              className={`w-full p-2 border rounded-md ${theme.text} ${theme.background}`}
            />
          </div>
          <div>
            <label htmlFor="password" className={`block mb-1 ${theme.text}`}>Password</label>
            <input
              type="password"
              id="password"
              className={`w-full p-2 border rounded-md ${theme.text} ${theme.background}`}
            />
          </div>
          <button
            type="submit"
            className={`w-full ${theme.primary} px-4 py-2 rounded-md hover:opacity-90 transition-opacity`}
          >
            Login
          </button>
        </form>
      </div>
    </div>
  )
}

