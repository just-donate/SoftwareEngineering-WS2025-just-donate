// src/api/axiosInstance.js
import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

// Create an axios instance
const axiosInstance = axios.create({
  baseURL: API_URL, // Adjust your base URL accordingly
  withCredentials: true, // Allow cookies to be included in cross-origin requests
});

// Attach an interceptor to include the Bearer token if it exists
axiosInstance.interceptors.request.use(
  (config) => {
    const jwtToken = sessionStorage.getItem('token');

    if (jwtToken) {
      config.headers.Authorization = `Bearer ${jwtToken}`;
    }

    // Return the config unchanged if there is no token
    return config;
  },
  (error) => {
    // Pass errors in request configuration to the next error handler
    return Promise.reject(error);
  }
);

export default axiosInstance;
