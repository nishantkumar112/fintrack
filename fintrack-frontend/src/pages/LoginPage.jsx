import {useEffect, useState} from 'react';
import {Link, Navigate, useLocation, useNavigate} from 'react-router-dom';

import {FcGoogle} from 'react-icons/fc';

import {useAuth} from '../context/AuthContext';
import {useToast} from '../context/ToastContext';

import FormInput from '../components/FormInput';

const LoginPage = () => {
  const {login, isAuthenticated, bootstrapping} = useAuth();

  const {error: showError, success} = useToast();

  const location = useLocation();
  const navigate = useNavigate();

  const from = location.state?.from?.pathname || '/dashboard';

  const [form, setForm] = useState({
    email: '',
    password: '',
  });

  const [submitting, setSubmitting] = useState(false);

  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    const params = new URLSearchParams(location.search);

    const token = params.get('token');

    if (token) {
      localStorage.setItem('token', token);

      success('Google login successful');

      navigate('/dashboard', {
        replace: true,
      });
    }
  }, [location.search, navigate, success]);

  if (!bootstrapping && isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  const handleChange = (e) => {
    const {name, value} = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));

    setFieldErrors((prev) => ({
      ...prev,
      [name]: '',
    }));
  };

  const validateForm = () => {
    const errors = {};

    if (!form.email.trim()) {
      errors.email = 'Email is required';
    }

    if (!form.password.trim()) {
      errors.password = 'Password is required';
    }

    setFieldErrors(errors);

    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setSubmitting(true);

    try {
      await login({
        email: form.email.trim(),
        password: form.password,
      });

      navigate(from, {
        replace: true,
      });
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Login failed';

      showError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  return (
    <div className="rounded-3xl border border-slate-200/80 bg-white/90 p-8 shadow-2xl backdrop-blur dark:border-slate-800 dark:bg-slate-900/90">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
          Sign in
        </h1>

        <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
          Access your dashboard, analytics and transactions.
        </p>
      </div>

      {/* GOOGLE LOGIN */}

      <button
        type="button"
        onClick={handleGoogleLogin}
        className="
          flex w-full items-center justify-center gap-3
          rounded-2xl border border-slate-300 bg-white
          px-4 py-3 text-sm font-medium text-slate-700
          transition hover:bg-slate-50
          dark:border-slate-700 dark:bg-slate-900
          dark:text-slate-200 dark:hover:bg-slate-800
        "
      >
        <FcGoogle className="text-xl" />
        Continue with Google
      </button>

      {/* DIVIDER */}

      <div className="my-6 flex items-center gap-3">
        <div className="h-px flex-1 bg-slate-200 dark:bg-slate-700" />

        <span className="text-xs uppercase tracking-wide text-slate-400">
          OR
        </span>

        <div className="h-px flex-1 bg-slate-200 dark:bg-slate-700" />
      </div>

      {/* LOGIN FORM */}

      <form onSubmit={handleSubmit} className="space-y-5" noValidate>
        <FormInput
          label="Email"
          name="email"
          type="email"
          autoComplete="email"
          value={form.email}
          onChange={handleChange}
          error={fieldErrors.email}
          required
        />

        <FormInput
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={form.password}
          onChange={handleChange}
          error={fieldErrors.password}
          required
        />

        <div className="flex justify-end">
          <Link
            to="/forgot-password"
            className="
              text-sm font-medium text-indigo-600
              hover:underline dark:text-indigo-400
            "
          >
            Forgot password?
          </Link>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="
            w-full rounded-2xl bg-indigo-600
            py-3 text-sm font-semibold text-white
            shadow-lg transition
            hover:bg-indigo-500
            disabled:cursor-not-allowed
            disabled:opacity-60
          "
        >
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>

      <p className="mt-8 text-center text-sm text-slate-600 dark:text-slate-400">
        Don&apos;t have an account?{' '}
        <Link
          to="/signup"
          className="
            font-semibold text-indigo-600
            hover:underline dark:text-indigo-400
          "
        >
          Create account
        </Link>
      </p>
    </div>
  );
};

export default LoginPage;
