'use client'

import React, { createContext, useState, useContext, ReactNode, useEffect } from 'react'
import { Theme } from '../styles/themes'

interface ThemeContextType {
  theme: Theme
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [theme, setThemeState] = useState<Theme>({
    primary: 'bg-blue-600 text-white',
    secondary: 'bg-cyan-500 text-white',
    accent: 'bg-orange-400 text-gray-900',
    background: 'bg-gray-50',
    card: 'bg-white',
    text: 'text-gray-900',
    textLight: 'text-gray-700',
    font: 'font-sans',
    icon: '🌍',
    ngoName: 'World Aid',
    ngoUrl: 'https://www.worldaid.org',
    helpUrl: '/support',
    statusColors: {
      donated: 'bg-emerald-500',
      inTransit: 'bg-amber-500',
      allocated: 'bg-sky-500',
      used: 'bg-indigo-500',
    },
  })

  useEffect(() => {
    // Mock API call to fetch theme
    const fetchedTheme = {
      primary: 'bg-blue-600 text-white',
      secondary: 'bg-cyan-500 text-white',
      accent: 'bg-orange-400 text-gray-900',
      background: 'bg-gray-50',
      card: 'bg-white',
      text: 'text-gray-900',
      textLight: 'text-gray-700',
      font: 'font-sans',
      icon: '🌍',
      ngoName: 'World Aid',
      ngoUrl: 'https://www.worldaid.org',
      helpUrl: '/support',
      statusColors: {
        donated: 'bg-emerald-500',
        inTransit: 'bg-amber-500',
        allocated: 'bg-sky-500',
        used: 'bg-indigo-500',
      },
    }

    setThemeState(fetchedTheme)
  }, [])

  return (
      <ThemeContext.Provider value={{ theme }}>
        {children}
      </ThemeContext.Provider>
  )
}

export const useTheme = () => {
  const context = useContext(ThemeContext)
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider')
  }
  return context
}

