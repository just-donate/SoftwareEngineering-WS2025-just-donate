import { Donation } from '@/types/types';
import { StatusTimeline } from './StatusTimeline';
import { customStyles } from '@/styles/custom';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css'; // <-- Import Leaflet's default CSS

interface DonationDetailsProps {
  donation: Donation;
  onClose: () => void;
}

export const DonationDetails: React.FC<DonationDetailsProps> = ({ donation, onClose }) => {
  const position: [number, number] = [-3.315502, 40.016154];

  // Placeholder images
  const images = [
    'https://via.placeholder.com/300x200?text=Image+1',
    'https://via.placeholder.com/300x200?text=Image+2',
  ];

  return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
        <div
            className={`
          ${customStyles.layout.card}
          ${customStyles.colors.card}
          max-w-2xl w-full
          max-h-[90vh]
          overflow-y-auto
        `}
        >
          {/* Header */}
          <div className="flex justify-between items-center mb-4">
            <h2 className={customStyles.text.heading}>Donation Details</h2>
            <button
                onClick={onClose}
                className={`${customStyles.button.base} ${customStyles.button.secondary}`}
            >
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
              >
                <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          {/* Basic info in a grid */}
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <p className={`${customStyles.text.body} font-semibold`}>NGO:</p>
              <p className={customStyles.colors.text}>{donation.ngo}</p>
            </div>
            <div>
              <p className={`${customStyles.text.body} font-semibold`}>Project:</p>
              <p className={customStyles.colors.text}>{donation.project}</p>
            </div>
            <div>
              <p className={`${customStyles.text.body} font-semibold`}>Amount:</p>
              <p className={customStyles.colors.text}>
                {donation.amount} {donation.currency}
              </p>
            </div>
            <div>
              <p className={`${customStyles.text.body} font-semibold`}>Date:</p>
              <p className={customStyles.colors.text}>{donation.date}</p>
            </div>
          </div>

          {/* Timeline and images side by side */}
          <div className="grid grid-cols-2 gap-4">
            {/* Column 1: Status Timeline */}
            <div>
              <StatusTimeline status={donation.status} />
            </div>

            {/* Column 2: Two placeholder images */}
            <div>
              <p className={`${customStyles.text.body} font-semibold mb-2`}>Project Photos</p>
              <div className="grid grid-cols-2 gap-2">
                {images.map((url, i) => (
                    <img
                        key={i}
                        src={url}
                        alt={`Project photo ${i + 1}`}
                        className="w-full h-auto rounded shadow"
                    />
                ))}
              </div>
            </div>
          </div>

          {/* Map below */}
          <div className="my-4 w-full h-[400px]">
            <MapContainer
                center={position}
                zoom={3}
                scrollWheelZoom={false}
                style={{ width: '100%', height: '100%' }}
            >
              <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">
                OpenStreetMap</a> contributors'
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              <Marker position={position}>
                <Popup>A pretty CSS3 popup. <br /> Easily customizable.</Popup>
              </Marker>
            </MapContainer>
          </div>
        </div>
      </div>
  );
};

