'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useTheme } from '@/contexts/ThemeContext'
import { Navigation } from '@/components/tracking/Navigation'

export default function SearchPage() {
  const { theme } = useTheme()
  const router = useRouter()
  const [searchInput, setSearchInput] = useState('')
  const [email, setEmail] = useState('')
  const [isEmailSubmitted, setIsEmailSubmitted] = useState(false)
  const [dropdownVisible, setDropdownVisible] = useState(false)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchInput.startsWith('http://') || searchInput.startsWith('https://')) {
      window.location.href = searchInput
    } else {
      router.push(`/tracking?id=${searchInput}`)
    }
  }

  const handleForgotTrackingLink = () => {
    setDropdownVisible(!dropdownVisible)
  }

  const handleEmailSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log(`Email submitted: ${email}`)
    setIsEmailSubmitted(true)
  }

  return (
      <div className={`h-screen ${theme.background}`}>
        <Navigation links={[{ link: 'SoftwareEngineering-WS2025-just-donate/organization/', name: 'Organization' }]} />
        <main className={`${theme.text} max-w-4xl mx-auto px-4 flex flex-col items-center justify-center h-[calc(100%-4rem)]`}>
          <div className={`${theme.card} p-6 rounded-lg shadow-lg w-full max-w-2xl`}>
            <form onSubmit={handleSearch} className="flex items-center space-x-4 w-full">
              <input
                  type="text"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  placeholder="Enter your tracking link or ID"
                  className={`flex-grow p-2 border rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 ${theme.text} ${theme.background}`}
                  style={{ flex: '2 1 0%' }}
              />
              <button type="submit" className={`${theme.primary} px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap`} style={{ flex: '1 1 0%' }}>
                Search
              </button>
            </form>
            <div className={`relative mt-4 w-full transition-all duration-200 ${dropdownVisible ? 'h-auto' : 'h-12'}`}>
              <button
                  type="button"
                  onClick={handleForgotTrackingLink}
                  className="px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap w-full text-left text-blue-600 border border-blue-600 bg-white"
              >
                Forgot Tracking Link?
              </button>
              {dropdownVisible && (
                  <div className="mt-2 p-4 border rounded-md shadow-lg bg-white z-10">
                    <form onSubmit={handleEmailSubmit} className="flex items-center space-x-4 w-full">
                      <input
                          type="email"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          placeholder="Enter your email"
                          className={`flex-grow p-2 border rounded-md shadow-sm hover:shadow-md transition-shadow duration-200 ${theme.text} ${theme.background}`}
                          style={{ flex: '2 1 0%' }}
                      />
                      <button type="submit" className={`${theme.primary} px-4 py-2 rounded-md shadow-sm hover:shadow-md transition-all duration-200 hover:opacity-90 whitespace-nowrap`} style={{ flex: '1 1 0%' }}>
                        Submit
                      </button>
                    </form>
                  </div>
              )}
            </div>
            {isEmailSubmitted && (
                <p className={`${theme.textLight} mt-4`}>
                  Thank you! A link has been sent to your email.
                </p>
            )}
          </div>
        </main>
      </div>
  )
}

