import api from './api';

export const getNotifications = (page = 0, size = 10, unreadOnly = false) =>
  api.get('/notifications', {
    params: {page, size, unreadOnly},
  });

export const getUnreadCount = () => api.get('/notifications/unread-count');

export const markAsRead = (id) => api.put(`/notifications/${id}/read`);

export const markAllAsRead = () => api.put('/notifications/read-all');
