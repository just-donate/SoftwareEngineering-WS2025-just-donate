'use client'

import React, { createContext, useState, useContext, ReactNode, useEffect } from 'react'
import { Theme, themes } from '../styles/themes'

interface ThemeContextType {
  theme: Theme
  setTheme: (themeName: string) => void
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [theme, setThemeState] = useState<Theme>(themes.default)

  const setTheme = (themeName: string) => {
    const newTheme = themes[themeName as keyof typeof themes] || themes.default
    setThemeState(newTheme)
    if (typeof window !== 'undefined') {
      localStorage.setItem('theme', themeName)
    }
  }

  useEffect(() => {
// TODO: Make real api call
    // For now just mock data

    const fetchedTheme = {
      custom: {
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
    };



    if (typeof window !== 'undefined') {
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme && themes[savedTheme as keyof typeof themes]) {
        setThemeState(themes[savedTheme as keyof typeof themes])
      } else {
        setThemeState(fetchedTheme.custom)
        localStorage.setItem('theme', 'custom');
      }
    }
  }, [])

  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
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

