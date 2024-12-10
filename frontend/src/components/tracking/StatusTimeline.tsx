import { DonationStatus } from '../types/types';
import { useTheme } from '../contexts/ThemeContext';

interface StatusTimelineProps {
  status: DonationStatus[];
}

export const StatusTimeline: React.FC<StatusTimelineProps> = ({ status }) => {
  const { theme } = useTheme();

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'donated':
        return theme.statusColors.donated;
      case 'processed':
      case 'in transit':
        return theme.statusColors.inTransit;
      case 'allocated':
        return theme.statusColors.allocated;
      case 'in use':
      case 'used':
        return theme.statusColors.used;
      default:
        return 'bg-gray-200';
    }
  };

  return (
    <div className="mt-4">
      <h3 className={`text-lg font-semibold mb-2 ${theme.text}`}>Donation Timeline</h3>
      <ol className="relative border-l border-gray-200">
        {status.map((item, index) => (
          <li key={index} className="mb-10 ml-4">
            <div className={`absolute w-3 h-3 ${getStatusColor(item.status)} rounded-full mt-1.5 -left-1.5 border border-white`}></div>
            <time className={`mb-1 text-sm font-normal leading-none ${theme.textLight}`}>{item.date}</time>
            <h4 className={`text-lg font-semibold ${theme.text}`}>{item.status}</h4>
            <p className={`mb-4 text-base font-normal ${theme.textLight}`}>{item.description}</p>
          </li>
        ))}
      </ol>
    </div>
  );
};

