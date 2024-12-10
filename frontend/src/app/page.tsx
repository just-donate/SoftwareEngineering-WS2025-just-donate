'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useTheme } from '@/contexts/ThemeContext'
import { Navigation } from '@/components/tracking/Navigation'


type SearchType = 'tracking' | 'email'

export default function SearchPage() {
  const { theme } = useTheme()
  const router = useRouter()
  const [searchType, setSearchType] = useState<SearchType>('tracking')
  const [searchInput, setSearchInput] = useState('')
  const [email, setEmail] = useState('')
  const [isEmailSubmitted, setIsEmailSubmitted] = useState(false)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchInput.startsWith('http://') || searchInput.startsWith('https://')) {
      window.location.href = searchInput
    } else {
      router.push(`/tracking/${searchInput}`)
    }
  }

  const handleEmailSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // Here you would typically send the email to your backend
    console.log(`Email submitted: ${email}`)
    setIsEmailSubmitted(true)
  }

  return (
    <div className={`h-screen ${theme.background}`}>
      <Navigation links={[{ link: '/organization', name: 'Organization' }]} />
      <main className={`${theme.text} max-w-4xl mx-auto px-4 flex flex-col items-center justify-center h-[calc(100%-4rem)]`}>
        <div className={`inline-flex rounded-md shadow-sm ${theme.background} mb-4`} role="group">
          <button
            type="button"
            className={`px-4 py-2 text-sm font-medium rounded-l-md ${
              searchType === 'tracking' ? theme.primary : theme.secondary
            }`}
            onClick={() => setSearchType('tracking')}
          >
            Tracking Link
          </button>
          <button
            type="button"
            className={`px-4 py-2 text-sm font-medium rounded-r-md ${
              searchType === 'email' ? theme.primary : theme.secondary
            }`}
            onClick={() => setSearchType('email')}
          >
            Send Email
          </button>
        </div>

        <div className={`${theme.card} p-6 rounded-lg shadow-lg w-full max-w-2xl`}>
          {searchType === 'tracking' ? (
            <form onSubmit={handleSearch} className="flex items-center space-x-4">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Enter your tracking link or ID"
                className={`flex-grow p-2 border rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 ${theme.text} ${theme.background}`}
              />
              <button type="submit" className={`${theme.primary} px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap`}>
                Search
              </button>
            </form>
          ) : (
            <form onSubmit={handleEmailSubmit} className="flex items-center space-x-4">
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                className={`flex-grow p-2 border rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 ${theme.text} ${theme.background}`}
              />
              <button type="submit" className={`${theme.secondary} px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap`}>
                Send Link
              </button>
            </form>
          )}

          {searchType === 'email' && isEmailSubmitted && (
            <p className={`${theme.textLight} mt-4`}>
              Thank you! A link has been sent to your email.
            </p>
          )}
        </div>
      </main>
    </div>
  )
}

