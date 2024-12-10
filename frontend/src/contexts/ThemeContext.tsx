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
    if (typeof window !== 'undefined') {
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme && themes[savedTheme as keyof typeof themes]) {
        setThemeState(themes[savedTheme as keyof typeof themes])
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

