import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';

import {
  loginRequest,
  signupRequest,
  fetchCurrentUser,
  logoutRequest,
} from '../services/authService';

import {normalizeAuthPayload} from '../utils/jwt';
import {normalizeRole} from '../utils/rbac';

const AuthContext = createContext(null);

const readStoredUser = () => {
  try {
    const raw = localStorage.getItem('user');

    if (!raw) return null;

    const parsed = JSON.parse(raw);

    return {
      ...parsed,
      role: normalizeRole(parsed?.role),
    };
  } catch {
    return null;
  }
};

export const AuthProvider = ({children}) => {
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  const [refreshToken, setRefreshToken] = useState(() =>
    localStorage.getItem('refreshToken'),
  );

  const [user, setUser] = useState(() => readStoredUser());

  const [bootstrapping, setBootstrapping] = useState(Boolean(token));

  /*
   * BOOTSTRAP USER
   */

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
          const normalizedUser = {
            ...me,
            role: normalizeRole(me.role),
          };

          setUser(normalizedUser);

          localStorage.setItem('user', JSON.stringify(normalizedUser));
        }
      } catch (err) {
        console.error('Failed to bootstrap session', err);
      } finally {
        if (!cancelled) {
          setBootstrapping(false);
        }
      }
    };

    run();

    return () => {
      cancelled = true;
    };
  }, [token]);

  /*
   * SAVE SESSION
   */

  const persistSession = useCallback(
    (nextToken, nextRefreshToken, nextUser) => {
      /*
       * ACCESS TOKEN
       */

      if (nextToken) {
        localStorage.setItem('token', nextToken);
      } else {
        localStorage.removeItem('token');
      }

      /*
       * REFRESH TOKEN
       */

      if (nextRefreshToken) {
        localStorage.setItem('refreshToken', nextRefreshToken);
      } else {
        localStorage.removeItem('refreshToken');
      }

      /*
       * USER
       */

      if (nextUser) {
        const normalizedUser = {
          ...nextUser,
          role: normalizeRole(nextUser.role),
        };

        localStorage.setItem('user', JSON.stringify(normalizedUser));

        setUser(normalizedUser);
      } else {
        localStorage.removeItem('user');

        setUser(null);
      }

      setToken(nextToken || null);

      setRefreshToken(nextRefreshToken || null);
    },
    [],
  );

  /*
   * LOGIN
   */

  const login = useCallback(
    async (credentials) => {
      const response = await loginRequest(credentials);

      const normalized = normalizeAuthPayload(response);

      if (!normalized.token) {
        throw new Error('No token returned from server');
      }

      persistSession(
        normalized.token,
        normalized.refreshToken,
        normalized.user,
      );

      return normalized.user;
    },
    [persistSession],
  );

  const loginWithOAuth = useCallback(
    async ({token, refreshToken}) => {
      const me = await fetchCurrentUser(token);

      persistSession(token, refreshToken, me);

      return me;
    },
    [persistSession],
  );

  /*
   * SIGNUP
   */

  const signup = useCallback(
    async (payload) => {
      const response = await signupRequest(payload);

      const normalized = normalizeAuthPayload(response);

      if (normalized.token) {
        persistSession(
          normalized.token,
          normalized.refreshToken,
          normalized.user,
        );

        return {
          autoLoggedIn: true,
          user: normalized.user,
        };
      }

      return {
        autoLoggedIn: false,
      };
    },
    [persistSession],
  );

  /*
   * LOGOUT
   */

  const logout = useCallback(async () => {
    try {
      if (refreshToken) {
        await logoutRequest({
          refreshToken,
        });
      }
    } catch (err) {
      console.error('Logout API failed', err);
    } finally {
      persistSession(null, null, null);
    }
  }, [refreshToken, persistSession]);

  /*
   * CONTEXT VALUE
   */

  const value = useMemo(
    () => ({
      token,
      refreshToken,
      user,
      bootstrapping,

      login,
      loginWithOAuth,

      signup,
      logout,

      isAuthenticated: Boolean(token),
    }),
    [
      token,
      refreshToken,
      user,
      bootstrapping,
      login,
      loginWithOAuth,
      signup,
      logout,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
};
