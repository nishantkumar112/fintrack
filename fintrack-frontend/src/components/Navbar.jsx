import {Link, useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import {useTheme} from '../context/ThemeContext';
import {useState, useCallback, useEffect} from 'react';

import {
  getNotifications,
  getUnreadCount,
  markAsRead as markNotificationAsRead,
} from '../services/notificationService';

import {Bell, LogOut, Moon, Sun, Menu} from 'lucide-react';

const Navbar = ({onMenuClick}) => {
  const {user, logout} = useAuth();

  const {theme, toggleTheme} = useTheme();

  const navigate = useNavigate();

  const [open, setOpen] = useState(false);

  const [notifications, setNotifications] = useState([]);

  const [unreadCount, setUnreadCount] = useState(0);

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

  useEffect(() => {
    fetchUnreadCount();
  }, [fetchUnreadCount]);

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

      setOpen(false);

      if (notification.redirectUrl) {
        navigate(notification.redirectUrl);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleLogout = async () => {
    try {
      await logout();

      navigate('/login', {
        replace: true,
      });
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-950">
      <div className="flex items-center justify-between p-4">
        {/* LEFT */}

        <div className="flex items-center gap-3">
          <button
            onClick={onMenuClick}
            className="rounded-md p-2 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <Menu className="h-5 w-5" />
          </button>

          <Link to="/dashboard" className="text-lg font-bold">
            FinTrack
          </Link>
        </div>

        {/* RIGHT */}

        <div className="flex items-center gap-3">
          {/* NOTIFICATIONS */}

          <div
            className="relative"
            onMouseEnter={openDropdown}
            onMouseLeave={closeDropdown}
          >
            <button className="relative rounded-md p-2 hover:bg-slate-100 dark:hover:bg-slate-800">
              <Bell className="h-5 w-5" />

              {unreadCount > 0 && (
                <span className="absolute -right-1 -top-1 rounded-full bg-red-500 px-1.5 text-[10px] text-white">
                  {unreadCount}
                </span>
              )}
            </button>

            {open && (
              <div className="absolute right-0 mt-2 w-80 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xl dark:border-slate-700 dark:bg-slate-900">
                <div className="border-b p-3 font-semibold">Notifications</div>

                <div className="max-h-72 overflow-y-auto">
                  {notifications.length === 0 ? (
                    <div className="p-4 text-sm text-slate-500">
                      No notifications
                    </div>
                  ) : (
                    notifications.map((n) => (
                      <div
                        key={n.id}
                        onClick={() => handleNotificationClick(n)}
                        className={`cursor-pointer border-b p-3 transition hover:bg-slate-100 dark:hover:bg-slate-800 ${
                          !n.readStatus
                            ? 'bg-slate-50 dark:bg-slate-800/60'
                            : ''
                        }`}
                      >
                        <div className="text-sm font-semibold">{n.title}</div>

                        <div className="mt-1 text-xs text-slate-500">
                          {n.message}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* THEME */}

          <button
            onClick={toggleTheme}
            className="rounded-md p-2 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            {theme === 'dark' ? (
              <Sun className="h-5 w-5" />
            ) : (
              <Moon className="h-5 w-5" />
            )}
          </button>

          {/* USER */}

          <div className="hidden text-sm md:block">{user?.email}</div>

          {/* LOGOUT */}

          <button
            onClick={handleLogout}
            className="flex items-center gap-2 rounded-md bg-black px-3 py-2 text-sm text-white transition hover:bg-gray-900"
          >
            <LogOut className="h-4 w-4" />
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
