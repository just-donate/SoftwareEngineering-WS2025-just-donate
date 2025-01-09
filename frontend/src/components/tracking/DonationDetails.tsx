import { Donation } from '../../types/types';
import { StatusTimeline } from './StatusTimeline';
import { customStyles } from '../../styles/custom';
import { dateFormatter } from '@/lib/utils';

interface DonationDetailsProps {
  donation: Donation;
  onClose: () => void;
}

export const DonationDetails: React.FC<DonationDetailsProps> = ({ donation, onClose }) => {

  console.log(donation);
  console.log(donation.organisation);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
      <div className={`${customStyles.layout.card} ${customStyles.colors.card} max-w-2xl w-full max-h-[90vh] overflow-y-auto`}>
        <div className="flex justify-between items-center mb-4">
          <h2 className={customStyles.text.heading}>Donation Details</h2>
          <button onClick={onClose} className={`${customStyles.button.base} ${customStyles.button.secondary}`}>
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div className="grid grid-cols-2 gap-4 mb-4">
          <div>
            <p className={`${customStyles.text.body} font-semibold`}>NGO:</p>
            <p className={customStyles.colors.text}>{donation.organisation}</p>
          </div>
          <div>
            <p className={`${customStyles.text.body} font-semibold`}>Project:</p>
            <p className={customStyles.colors.text}>{donation.earmarking || "General Purpose Donation"}</p>
          </div>
          <div>
            <p className={`${customStyles.text.body} font-semibold`}>Amount:</p>
            <p className={customStyles.colors.text}>{donation.amount.amount} Euro</p>
          </div>
          <div>
            <p className={`${customStyles.text.body} font-semibold`}>Date:</p>
            <p className={customStyles.colors.text}>{dateFormatter.format(new Date(donation.date))}</p>
          </div>
        </div>
        <StatusTimeline status={donation.status} />
      </div>
    </div>
  );
};

