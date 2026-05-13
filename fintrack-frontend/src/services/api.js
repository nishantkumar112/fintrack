import axios from 'axios';

const resolveBaseURL = () => {
  const explicit = String(import.meta.env.VITE_API_BASE_URL || '').trim();
  const useDirectInDev = import.meta.env.VITE_USE_DIRECT_API === 'true';

  if (import.meta.env.DEV) {
    if (!useDirectInDev) return '/api';
    return explicit || 'http://localhost:8080';
  }

  if (explicit) return explicit;

  if (import.meta.env.VITE_DISABLE_API_PROXY === 'true') {
    return 'http://localhost:8080';
  }

  if (typeof window !== 'undefined') {
    const host = window.location.hostname;
    const port = window.location.port;
    if (
      host === 'localhost' ||
      host === '127.0.0.1' ||
      port === '5173' ||
      port === '4173'
    ) {
      return '/api';
    }
  }

  return 'http://localhost:8080';
};

const baseURL = resolveBaseURL();

const api = axios.create({
  baseURL,
  headers: {'Content-Type': 'application/json'},
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');

        const response = await axios.post('/auth/refresh', {refreshToken});

        const newAccessToken = response.data.accessToken;

        localStorage.setItem('token', newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        return api(originalRequest);
      } catch (error) {
        console.log(error);
        localStorage.clear();
        window.location.assign('/login');
      }
    }

    return Promise.reject(error);
  },
);

export default api;
