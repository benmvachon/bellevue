import React from 'react';
import { useNavigate } from 'react-router-dom';
import ImageButton from './ImageButton';

function NoAuthHeader() {
  const navigate = useNavigate();

  return (
    <div className="header no-auth">
      <ImageButton name="move-in" onClick={() => navigate('/signup')}>
        <span>sign up</span>
      </ImageButton>
      <ImageButton name="house" onClick={() => navigate('/login')}>
        <span>log in</span>
      </ImageButton>
    </div>
  );
}

NoAuthHeader.displayName = 'NoAuthHeader';

export default NoAuthHeader;
