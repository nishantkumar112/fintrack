import api from "./api";
import { normalizeAuthPayload } from "../utils/jwt";

export const loginRequest = async (credentials) => {
  const res = await api.post("/auth/login", credentials);
  return normalizeAuthPayload(res.data);
};

export const signupRequest = async (payload) => {
  const res = await api.post("/auth/signup", payload);
  if (res.data?.accessToken || res.data?.token) {
    return normalizeAuthPayload(res.data);
  }
  return res.data;
};

export const fetchCurrentUser = async () => {
  const res = await api.get("/auth/me");
  return res.data;
};
