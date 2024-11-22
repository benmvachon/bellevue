import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';

function LoginPage() {
  const navigate = useNavigate();
  const { handleLogin, handleLogout, isAuthenticated } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

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
        setLoading(false);
        navigate('/profile/1');
      },
      () => {
        setError('Invalid username or password');
        setLoading(false);
      }
    );
  };

  return (
    <div className="page login-page">
      <h1>Login</h1>
      <form onSubmit={onSubmit}>
        <div>
          <label htmlFor="username">Username:</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        <div>
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" disabled={loading}>
          Login
        </button>
      </form>
    </div>
  );
}

export default LoginPage;
