import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';
import { signup } from '../api/api.js';

function SignupPage() {
  const navigate = useNavigate();
  const { handleLogin, handleLogout, isAuthenticated } = useAuth();
  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [avatar, setAvatar] = useState();
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated) handleLogout();
  }, [isAuthenticated, handleLogout]);

  const onSubmit = (event) => {
    setLoading(true);
    event.preventDefault();
    signup(
      name,
      username,
      password,
      avatar,
      (response) => {
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
      },
      (err) => {
        setError(err);
        setLoading(false);
      }
    );
  };

  return (
    <div className="page signup-page">
      <h1>Signup</h1>
      <form onSubmit={onSubmit}>
        <div>
          <label htmlFor="name">Name:</label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            disabled={loading}
          />
        </div>
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
        <div>
          <label htmlFor="avatar">Favorite food:</label>
          <select
            id="avatar"
            name="avatar"
            value={avatar}
            onChange={(e) => setAvatar(e.target.value)}
          >
            <option value="cat">Pizza</option>
            <option value="raptor">Steak</option>
            <option value="walrus">Sushi</option>
            <option value="bee">Cake</option>
            <option value="monkey">Smoothie</option>
            <option value="horse">Salad</option>
          </select>
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" disabled={loading}>
          Sign up
        </button>
      </form>
    </div>
  );
}

SignupPage.displayName = 'SignupPage';

export default SignupPage;
