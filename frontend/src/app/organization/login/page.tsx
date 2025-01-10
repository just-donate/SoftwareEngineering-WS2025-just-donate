'use client';

import { useTheme } from '@/contexts/ThemeContext';

export default function LoginPage() {
  const { theme } = useTheme();

  return (
    <div
      className={`flex items-center justify-center min-h-screen ${theme.background}`}
    >
      <div className={`${theme.card} p-8 rounded-lg shadow-lg w-full max-w-md`}>
        <h1 className={`text-2xl font-bold mb-6 ${theme.text}`}>
          Organization Login
        </h1>
        <form className="space-y-4">
          <div>
            <label htmlFor="email" className={`block mb-1 ${theme.text}`}>
              Email
            </label>
            <input
              type="email"
              id="email"
              className={`w-full p-2 border rounded-md ${theme.text} ${theme.background}`}
            />
          </div>
          <div>
            <label htmlFor="password" className={`block mb-1 ${theme.text}`}>
              Password
            </label>
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
  );
}
