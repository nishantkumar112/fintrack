import {Link, useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {useTheme} from '../context/ThemeContext';
import {useEffect, useState, useCallback} from 'react';
import {
  getNotifications,
  getUnreadCount,
  markAsRead as markNotificationAsRead,
} from '../services/notificationService';

import {Bell} from 'lucide-react';

const Navbar = ({onMenuClick}) => {
  const {user, logout} = useAuth();
  const {theme, toggleTheme} = useTheme();
  const navigate = useNavigate();

  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const handleLogout = () => logout();

  const fetchUnreadCount = useCallback(async () => {
    try {
      const res = await getUnreadCount();
      setUnreadCount(res?.data ?? 0);
    } catch {
      setUnreadCount(0);
    }
  }, []);

  const fetchNotifications = useCallback(async () => {
    try {
      const res = await getNotifications(0, 10, false);
      setNotifications(res?.data?.content ?? []);
    } catch {
      setNotifications([]);
    }
  }, []);

  const openDropdown = async () => {
    setOpen(true);
    await fetchNotifications();
  };

  const closeDropdown = () => {
    setOpen(false);
  };

  const handleNotificationClick = async (notification) => {
    try {
      await markNotificationAsRead(notification.id);

      setNotifications((prev) =>
        prev.map((n) =>
          n.id === notification.id ? {...n, readStatus: true} : n,
        ),
      );

      setUnreadCount((prev) => Math.max(prev - 1, 0));

      if (notification.redirectUrl) {
        navigate(notification.redirectUrl);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
  }, [fetchUnreadCount]);

  return (
    <header className="sticky top-0 z-40 border-b bg-white dark:bg-slate-950">
      <div className="flex justify-between items-center p-4">
        {/* LEFT */}
        <div className="flex items-center gap-3">
          <button onClick={onMenuClick}>☰</button>

          <Link to="/dashboard" className="font-bold">
            FinTrack
          </Link>
        </div>

        {/* RIGHT */}
        <div className="flex items-center gap-4 relative">
          {/* NOTIFICATIONS */}
          <div
            className="relative"
            onMouseEnter={openDropdown}
            onMouseLeave={closeDropdown}
          >
            <button className="relative p-2 rounded-md hover:bg-gray-100 dark:hover:bg-slate-800">
              <Bell className="w-5 h-5 text-slate-700 dark:text-slate-200" />

              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] px-1.5 rounded-full">
                  {unreadCount}
                </span>
              )}
            </button>

            {open && (
              <div className="absolute right-0 mt-2 w-80 bg-white dark:bg-slate-900 shadow-lg border rounded-lg overflow-hidden">
                <div className="p-3 border-b font-semibold">Notifications</div>

                <div className="max-h-64 overflow-y-auto">
                  {notifications.length === 0 ? (
                    <div className="p-3 text-sm text-gray-500">
                      No notifications
                    </div>
                  ) : (
                    notifications.map((n) => (
                      <div
                        key={n.id}
                        onClick={() => handleNotificationClick(n)}
                        className={`p-3 cursor-pointer border-b hover:bg-gray-100 dark:hover:bg-slate-800 ${
                          !n.readStatus ? 'bg-gray-50 dark:bg-slate-800' : ''
                        }`}
                      >
                        <div className="font-medium text-sm">{n.title}</div>
                        <div className="text-xs text-gray-500">{n.message}</div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* THEME */}
          <button onClick={toggleTheme}>
            {theme === 'dark' ? 'Light' : 'Dark'}
          </button>

          {/* USER */}
          <span>{user?.email || 'User'}</span>

          {/* LOGOUT */}
          <button onClick={handleLogout}>Logout</button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
