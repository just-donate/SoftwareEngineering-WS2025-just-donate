import { DonationStatus } from '../../types/types';
import { useTheme } from '../../contexts/ThemeContext';
import { capitalize, dateFormatter } from '@/lib/utils';
import { getStatusColor } from '@/lib/status';

interface StatusTimelineProps {
  status: DonationStatus[];
}

export const StatusTimeline: React.FC<StatusTimelineProps> = ({ status }) => {
  const { theme } = useTheme();

  return (
    <div className="mt-4">
      <h3 className={`text-lg font-semibold mb-2 ${theme.text}`}>Donation Timeline</h3>
      <ol className="relative border-l border-gray-200">
        {status.map((item, index) => (
          <li key={index} className="mb-10 ml-4">
            <div className={`absolute w-3 h-3 ${getStatusColor(item.status, theme)} rounded-full mt-1.5 -left-1.5 border border-white`}></div>
            <time className={`mb-1 text-sm font-normal leading-none ${theme.textLight}`}>{dateFormatter.format(new Date(item.date))}</time>
            <h4 className={`text-lg font-semibold ${theme.text}`}>{capitalize(item.status)}</h4>
            <p className={`mb-4 text-base font-normal ${theme.textLight}`}>{item.description}</p>
          </li>
        ))}
      </ol>
    </div>
  );
};

