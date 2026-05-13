import api from './api';

export const loginRequest = async (payload) => {
  const response = await api.post('/auth/login', payload);

  return response.data;
};

export const signupRequest = async (payload) => {
  const response = await api.post('/auth/signup', payload);

  return response.data;
};

export const fetchCurrentUser = async (tokenOverride) => {
  const config = tokenOverride
    ? {
        headers: {
          Authorization: `Bearer ${tokenOverride}`,
        },
      }
    : {};

  const response = await api.get('/auth/me', config);

  return response.data;
};

export const logoutRequest = async (payload) => {
  return api.post('/auth/logout', payload);
};
