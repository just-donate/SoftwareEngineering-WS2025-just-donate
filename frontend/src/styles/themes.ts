export interface Theme {
  primary: string;
  secondary: string;
  accent: string;
  background: string;
  card: string;
  text: string;
  textLight: string;
  font: string;
  icon: string;
  ngoName: string;
  ngoUrl: string;
  helpUrl: string;
  statusColors: {
    donated: string;
    inTransit: string;
    allocated: string;
    used: string;
  };
}

export const themes: Record<string, Theme> = {
  default: {
    primary: 'bg-purple-600 text-white',
    secondary: 'bg-pink-500 text-white',
    accent: 'bg-yellow-400 text-gray-900',
    background: 'bg-gray-100',
    card: 'bg-white',
    text: 'text-gray-800',
    textLight: 'text-gray-600',
    font: 'font-sans',
    icon: '🎗️',
    ngoName: 'Global Giving',
    ngoUrl: 'https://www.globalgiving.org',
    helpUrl: '/help',
    statusColors: {
      donated: 'bg-green-500',
      inTransit: 'bg-yellow-500',
      allocated: 'bg-blue-500',
      used: 'bg-purple-500',
    },
  },
};

