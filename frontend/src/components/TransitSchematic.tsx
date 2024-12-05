'use client'

import React, { useState } from 'react';
import { useTheme } from '../contexts/ThemeContext';
import { DonationStatus } from '../types/types';

interface TransitSchematicProps {
  status: DonationStatus[];
}

export const TransitSchematic: React.FC<TransitSchematicProps> = ({ status }) => {
  const { theme } = useTheme();
  const currentStepIndex = status.length - 1;
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

  // Extract the primary color from the theme
  const primaryColor = theme.primary.split(' ')[0]; // Assumes the first class is the background color

  // Create an array of 5 steps, filling in with empty steps if needed
  const steps = [...status, ...Array(5 - status.length).fill({ status: '', description: '' })];

  return (
    <div className="flex items-center space-x-2 relative">
      {steps.map((step, index) => (
        <React.Fragment key={index}>
          <div 
            className={`w-3 h-3 rounded-full ${
              index <= currentStepIndex ? primaryColor : 'bg-gray-300'
            } relative cursor-pointer`}
            onMouseEnter={() => setHoveredIndex(index)}
            onMouseLeave={() => setHoveredIndex(null)}
          >
            {hoveredIndex === index && step.status && (
              <div className={`absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-2 py-1 text-xs ${theme.card} ${theme.text} rounded shadow-lg whitespace-nowrap z-10`}>
                {step.status}: {step.description}
              </div>
            )}
          </div>
          {index < 4 && (
            <div 
              className={`w-4 h-0.5 ${
                index < currentStepIndex ? primaryColor : 'bg-gray-300'
              }`}
            />
          )}
        </React.Fragment>
      ))}
    </div>
  );
};

