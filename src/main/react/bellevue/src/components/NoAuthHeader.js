import React from 'react';
import { useNavigate } from 'react-router-dom';

function NoAuthHeader() {
  const navigate = useNavigate();

  return (
    <div className="header no-auth">
      <button onClick={() => navigate('/signup')}>sign up</button>
      <button onClick={() => navigate('/login')}>log in</button>
    </div>
  );
}

NoAuthHeader.displayName = 'NoAuthHeader';

export default NoAuthHeader;
