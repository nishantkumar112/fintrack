import {useState} from 'react';

import {Link, Navigate, useNavigate} from 'react-router-dom';

import {FcGoogle} from 'react-icons/fc';

import {useAuth} from '../context/AuthContext';
import {useToast} from '../context/ToastContext';

import FormInput from '../components/FormInput';

const SignupPage = () => {
  const {signup, isAuthenticated, bootstrapping} = useAuth();

  const {success, error: showError} = useToast();

  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

  const [submitting, setSubmitting] = useState(false);

  const [fieldErrors, setFieldErrors] = useState({});

  if (!bootstrapping && isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
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

    if (!form.firstName.trim()) {
      errors.firstName = 'First name is required';
    }

    if (!form.lastName.trim()) {
      errors.lastName = 'Last name is required';
    }

    if (!form.email.trim()) {
      errors.email = 'Email is required';
    }

    if (!form.password.trim()) {
      errors.password = 'Password is required';
    } else if (form.password.length < 6) {
      errors.password = 'Password must be at least 6 characters';
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
      const result = await signup({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
      });

      if (result?.autoLoggedIn) {
        success('Account created successfully');

        navigate('/dashboard', {
          replace: true,
        });
      } else {
        success('Signup successful. Please login.');

        navigate('/login', {
          replace: true,
        });
      }
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Signup failed';

      showError(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleGoogleSignup = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  return (
    <div className="rounded-3xl border border-slate-200/80 bg-white/90 p-8 shadow-2xl backdrop-blur dark:border-slate-800 dark:bg-slate-900/90">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
          Create account
        </h1>

        <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
          Create your FinTrack account to manage expenses and analytics.
        </p>
      </div>

      {/* GOOGLE */}

      <button
        type="button"
        onClick={handleGoogleSignup}
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

      {/* FORM */}

      <form onSubmit={handleSubmit} className="space-y-5" noValidate>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <FormInput
            label="First name"
            name="firstName"
            value={form.firstName}
            onChange={handleChange}
            error={fieldErrors.firstName}
            required
          />

          <FormInput
            label="Last name"
            name="lastName"
            value={form.lastName}
            onChange={handleChange}
            error={fieldErrors.lastName}
            required
          />
        </div>

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
          autoComplete="new-password"
          value={form.password}
          onChange={handleChange}
          error={fieldErrors.password}
          required
        />

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
          {submitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <p className="mt-8 text-center text-sm text-slate-600 dark:text-slate-400">
        Already have an account?{' '}
        <Link
          to="/login"
          className="
            font-semibold text-indigo-600
            hover:underline dark:text-indigo-400
          "
        >
          Sign in
        </Link>
      </p>
    </div>
  );
};

export default SignupPage;
