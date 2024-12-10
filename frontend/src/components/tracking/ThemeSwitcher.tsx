import React from 'react';
import { useTheme } from '../../contexts/ThemeContext';
import { themes } from '../../styles/themes';

export const ThemeSwitcher: React.FC = () => {
  const { theme, setTheme } = useTheme();

  return (
    <select
      onChange={(e) => setTheme(e.target.value)}
      value={Object.keys(themes).find(key => themes[key] === theme) || 'default'}
      className="bg-white text-gray-800 border border-gray-300 rounded-md shadow-sm py-1 px-2 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-100 focus:ring-indigo-500 text-sm"
    >
      <option value="default">Default Theme</option>
      <option value="blue">Blue Theme</option>
      <option value="paleGray">Pale Gray Theme</option>
      <option value="paleBlue">Pale Blue Theme</option>
    </select>
  );
};

