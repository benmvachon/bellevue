import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';
import { signup } from '../api/api.js';

function SignupPage() {
  const navigate = useNavigate();
  const { handleLogin, handleLogout, isAuthenticated } = useAuth();
  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { setClassName } = useOutletContext();

  useEffect(() => {
    setClassName('signup-page');
  });

  useEffect(() => {
    if (isAuthenticated) handleLogout();
  }, [isAuthenticated, handleLogout]);

  const onSubmit = (event) => {
    setLoading(true);
    event.preventDefault();
    signup(
      name,
      username,
      email,
      password,
      (response) => {
        handleLogin(
          username,
          password,
          () => {
            setLoading(false);
            navigate('/');
          },
          () => {
            setError('Invalid username or password');
            setLoading(false);
          }
        );
      },
      (err) => {
        setError(err);
        setLoading(false);
      }
    );
  };

  return (
    <div className="page-contents">
      <h1>Signup</h1>
      <form onSubmit={onSubmit}>
        <div className="section">
          <label htmlFor="name">name:</label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={loading}
            autoComplete="off"
          />
        </div>
        <div className="section">
          <label htmlFor="password">email:</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
            autoComplete="off"
          />
        </div>
        <div className="section">
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
        <div className="section">
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
          sign up
        </button>
      </form>
    </div>
  );
}

SignupPage.displayName = 'SignupPage';

export default SignupPage;
