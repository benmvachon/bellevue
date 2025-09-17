import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';

function LoginPage() {
  const navigate = useNavigate();
  const { handleLogin, handleLogout, isAuthenticated } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { setClassName } = useOutletContext();

  useEffect(() => {
    setClassName('login-page');
  });

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
        setError(`Invalid username or password: ${error}`);
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
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" disabled={loading}>
          login
        </button>
      </form>
    </div>
  );
}

LoginPage.displayName = 'LoginPage';

export default LoginPage;
