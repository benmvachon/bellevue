import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';
import Button from '../components/Button';

function LoginPage() {
  const navigate = useNavigate();
  const { setClassName, pushAlert } = useOutletContext();
  const { handleLogin, handleLogout, isAuthenticated } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setClassName('login-page');
  }, [setClassName]);

  useEffect(() => {
    if (error) {
      pushAlert({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Failed Login Attempt: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [pushAlert, error]);

  useEffect(() => {
    if (isAuthenticated) handleLogout();
  }, [isAuthenticated, handleLogout]);

  const onSubmit = (event) => {
    setLoading(true);
    event.preventDefault();
    handleLogin(
      username,
      password,
      () => {
        navigate('/');
      },
      (error) => {
        setError(error);
        setLoading(false);
      }
    );
  };

  return (
    <div className="page-contents">
      <h1>Login</h1>
      <form onSubmit={onSubmit}>
        <div>
          <label htmlFor="username">username:</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            disabled={loading}
            autoComplete="off"
          />
        </div>
        <div>
          <label htmlFor="password">password:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
            autoComplete="off"
          />
        </div>
        <Button type="submit" disabled={loading}>
          login
        </Button>
      </form>
    </div>
  );
}

LoginPage.displayName = 'LoginPage';

export default LoginPage;
