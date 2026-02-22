import axios from 'axios';
import toast from 'react-hot-toast';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const message =
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      'Something went wrong';
    
    // Only show toast for non-network errors to avoid spam when backend is down
    if (!err.message.includes('Network Error') && !err.message.includes('ECONNREFUSED')) {
      toast.error(message);
    }
    
    console.error('API Error:', err);
    return Promise.reject(err);
  }
);

export default api;
