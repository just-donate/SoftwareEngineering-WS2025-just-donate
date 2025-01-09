// jest.config.ts
import type { Config } from 'jest';

const config: Config = {
  testEnvironment: 'jest-environment-jsdom',
  collectCoverage: true,
  collectCoverageFrom: ['src/**/*.{ts,tsx}', '!src/**/*.d.ts', '!src/**/*.config.ts'],

  // Use ts-jest to transform .ts or .tsx files
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest',
  },

  // Typically "v8" is a good choice for coverage with TS
  coverageProvider: 'v8',
};

export default config;