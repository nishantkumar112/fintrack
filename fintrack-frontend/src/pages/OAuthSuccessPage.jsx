import {useEffect} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';

const OAuthSuccessPage = () => {
  const navigate = useNavigate();

  const {loginWithOAuth} = useAuth();

  const [params] = useSearchParams();

  useEffect(() => {
    const handleOAuthLogin = async () => {
      try {
        const token = params.get('token');

        const refreshToken = params.get('refreshToken');

        if (!token) {
          navigate('/login');

          return;
        }

        await loginWithOAuth({
          token,
          refreshToken,
        });

        navigate('/dashboard', {
          replace: true,
        });
      } catch (err) {
        console.error('OAuth login failed', err);

        navigate('/login');
      }
    };

    handleOAuthLogin();
  }, [params, navigate, loginWithOAuth]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-sm text-slate-500">Signing you in...</div>
    </div>
  );
};

export default OAuthSuccessPage;
