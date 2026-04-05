import axios from "axios";

/**
 * Base URL for API calls.
 *
 * CORS: cross-origin calls need matching CORS headers on the API. Same-origin
 * `/api` + Vite proxy avoids that during local dev/preview.
 *
 * - **Development**: default `/api` (proxy). Set `VITE_USE_DIRECT_API=true` to
 *   call the API directly; then `VITE_API_BASE_URL` or `http://localhost:8080`
 *   is used (CORS must be allowed on the server).
 * - **Production build**: set `VITE_API_BASE_URL` at build time, or the app
 *   falls back to `http://localhost:8080` when not on localhost/preview ports.
 * - **Preview**: `/api` when the page is localhost / 127.0.0.1 or port 5173 / 4173.
 */
const resolveBaseURL = () => {
  const explicit = String(import.meta.env.VITE_API_BASE_URL || "").trim();
  const useDirectInDev = import.meta.env.VITE_USE_DIRECT_API === "true";

  if (import.meta.env.DEV) {
    if (!useDirectInDev) return "/api";
    return explicit || "http://localhost:8080";
  }

  if (explicit) return explicit;

  if (import.meta.env.VITE_DISABLE_API_PROXY === "true") {
    return "http://localhost:8080";
  }

  const onLocalhost =
    typeof window !== "undefined" &&
    (window.location.hostname === "localhost" ||
      window.location.hostname === "127.0.0.1");

  const port = typeof window !== "undefined" ? window.location.port : "";
  const viteDefaultPort = port === "5173" || port === "4173";

  if (onLocalhost || viteDefaultPort) {
    return "/api";
  }

  return "http://localhost:8080";
};

const baseURL = resolveBaseURL();

const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (!window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  }
);

export default api;
