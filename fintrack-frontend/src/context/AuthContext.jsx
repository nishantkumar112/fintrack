import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { loginRequest, signupRequest, fetchCurrentUser } from "../services/authService";
import { normalizeAuthPayload } from "../utils/jwt";
import { normalizeRole } from "../utils/rbac";

const AuthContext = createContext(null);

const readStoredUser = () => {
  try {
    const raw = localStorage.getItem("user");
    if (!raw) return null;
    const u = JSON.parse(raw);
    return { ...u, role: normalizeRole(u?.role) };
  } catch {
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [user, setUser] = useState(() => readStoredUser());
  const [bootstrapping, setBootstrapping] = useState(Boolean(token));

  useEffect(() => {
    if (!token) {
      setBootstrapping(false);
      return;
    }
    let cancelled = false;
    const run = async () => {
      try {
        const me = await fetchCurrentUser();
        if (!cancelled && me) {
          setUser({ ...me, role: normalizeRole(me.role) });
          localStorage.setItem("user", JSON.stringify({ ...me, role: normalizeRole(me.role) }));
        }
      } catch {
        /* keep user from storage / JWT */
      } finally {
        if (!cancelled) setBootstrapping(false);
      }
    };
    run();
    return () => {
      cancelled = true;
    };
  }, [token]);

  const persistSession = useCallback((nextToken, nextUser) => {
    if (nextToken) localStorage.setItem("token", nextToken);
    else localStorage.removeItem("token");
    if (nextUser) {
      const normalized = { ...nextUser, role: normalizeRole(nextUser.role) };
      localStorage.setItem("user", JSON.stringify(normalized));
      setUser(normalized);
    } else {
      localStorage.removeItem("user");
      setUser(null);
    }
    setToken(nextToken || null);
  }, []);

  const login = useCallback(async (credentials) => {
    const { token: t, user: u } = await loginRequest(credentials);
    if (!t) throw new Error("No token returned from server");
    persistSession(t, u);
    return u;
  }, [persistSession]);

  const signup = useCallback(async (payload) => {
    const data = await signupRequest(payload);
    const normalized = normalizeAuthPayload(data);
    if (normalized.token) {
      persistSession(normalized.token, normalized.user);
      return { autoLoggedIn: true, user: normalized.user };
    }
    return { autoLoggedIn: false };
  }, [persistSession]);

  const logout = useCallback(() => {
    persistSession(null, null);
  }, [persistSession]);

  const value = useMemo(
    () => ({
      token,
      user,
      bootstrapping,
      login,
      signup,
      logout,
      isAuthenticated: Boolean(token),
    }),
    [token, user, bootstrapping, login, signup, logout]
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
