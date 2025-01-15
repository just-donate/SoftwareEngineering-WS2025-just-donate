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
    announced: string;
    pending_confirmation: string;
    confirmed: string;
    received: string;
    in_transfer: string;
    processing: string;
    allocated: string;
    awaiting_utilization: string;
    used: string;
  };
  emailTemplates: {
    donationTemplate: string;
    withdrawalTemplate: string;
    manualTemplate: string;
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
      announced: 'bg-green-500',
      pending_confirmation: 'bg-yellow-500',
      confirmed: 'bg-blue-500',
      received: 'bg-purple-500',
      in_transfer: 'bg-yellow-500',
      processing: 'bg-blue-500',
      allocated: 'bg-purple-500',
      awaiting_utilization: 'bg-yellow-500',
      used: 'bg-green-500',
    },
    emailTemplates: {
      donationTemplate: `Thank you for your donation, to track your progress visit
{{tracking-link-with-id}}
or enter your tracking id
{{tracking-id}}
on our tracking page
{{tracking-link}}`,
      withdrawalTemplate: `Your recent donation to {{organisation-name}} has been fully utilized.
To see more details about the status of your donation, visit the following link
{{tracking-link-with-id}}
or enter your tracking id
{{tracking-id}}
on our tracking page
{{tracking-url}}`,
      manualTemplate: `Your donation to {{organisation-name}} has not been forgotten.
To track your progress visit
{{tracking-link-with-id}}
or enter your tracking id
{{tracking-id}}
on our tracking page
{{tracking-link}}`,
    },
  },
};
