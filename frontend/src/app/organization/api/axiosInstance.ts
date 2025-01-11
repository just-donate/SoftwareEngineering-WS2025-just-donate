// src/api/axiosInstance.js
import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_URL) {
  throw new Error('NEXT_PUBLIC_API_URL is not set');
}

// Create an axios instance
const axiosInstance = axios.create({
  baseURL: API_URL, // Adjust your base URL accordingly
  // You can add default headers here if needed, or add them dynamically
});

// Optional: Add an interceptor to attach the token to each request dynamically
// axiosInstance.interceptors.request.use(
//   (config) => {
//     const jwtToken = localStorage.getItem('jwtToken');

//     if (jwtToken) {
//       config.headers.Authorization = `Bearer ${jwtToken}`;
//     }
//     return config;
//   },
//   (error) => Promise.reject(error)
// );

export default axiosInstance;
