import React from 'react';
import { useNavigate } from 'react-router-dom';

function NoAuthHeader() {
  const navigate = useNavigate();

  return (
    <div className="header">
      <h1>BLORVIS</h1>
      <button onClick={() => navigate('/signup')}>Sign Up</button>
      <button onClick={() => navigate('/login')}>Log In</button>
    </div>
  );
}

NoAuthHeader.displayName = 'NoAuthHeader';

export default NoAuthHeader;
