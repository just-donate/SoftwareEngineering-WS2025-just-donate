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
  blue: {
    primary: 'bg-blue-600 text-white',
    secondary: 'bg-cyan-500 text-white',
    accent: 'bg-orange-400 text-gray-900',
    background: 'bg-gray-50',
    card: 'bg-white',
    text: 'text-gray-900',
    textLight: 'text-gray-700',
    font: 'font-serif',
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
  },
  paleGray: {
    primary: 'bg-gray-400 text-white',
    secondary: 'bg-gray-300 text-gray-800',
    accent: 'bg-gray-200 text-gray-800',
    background: 'bg-gray-50',
    card: 'bg-white',
    text: 'text-gray-700',
    textLight: 'text-gray-500',
    font: 'font-sans',
    icon: '🕊️',
    ngoName: 'Peace Foundation',
    ngoUrl: 'https://www.peacefoundation.org',
    helpUrl: '/assistance',
    statusColors: {
      donated: 'bg-gray-300',
      inTransit: 'bg-gray-400',
      allocated: 'bg-gray-500',
      used: 'bg-gray-600',
    },
  },
  paleBlue: {
    primary: 'bg-blue-300 text-white',
    secondary: 'bg-blue-200 text-gray-800',
    accent: 'bg-blue-100 text-gray-800',
    background: 'bg-blue-50',
    card: 'bg-white',
    text: 'text-gray-800',
    textLight: 'text-gray-600',
    font: 'font-sans',
    icon: '💧',
    ngoName: 'Ocean Care',
    ngoUrl: 'https://www.oceancare.org',
    helpUrl: '/support',
    statusColors: {
      donated: 'bg-blue-200',
      inTransit: 'bg-blue-300',
      allocated: 'bg-blue-400',
      used: 'bg-blue-500',
    },
  },
};

