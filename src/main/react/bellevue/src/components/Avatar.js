import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getProfile } from '../api/api.js';
import ImageButton from './ImageButton.js';

function Avatar({
  userId,
  userProp,
  size = 'small',
  flip = false,
  name = true,
  className
}) {
  const navigate = useNavigate();
  const [user, setUser] = useState({ ...userProp });
  const [error, setError] = useState(false);

  useEffect(() => {
    if (userId < 1) {
      setUser({ id: 0, avatar: 'cat' });
    } else if (userProp) {
      setUser(userProp);
    } else {
      getProfile(userId, setUser, setError);
    }
  }, [userId, userProp, setUser]);

  if (error) return JSON.stringify(error);

  return (
    <div className={`avatar ${className}${userId < 1 ? ' system' : ''}`}>
      <ImageButton
        name={user?.avatar}
        hat={user?.equipment?.hat}
        size={size}
        flip={flip}
        onClick={() => user?.id > 0 && navigate(`/home/${user?.id}`)}
      >
        {name && <span className="name">{user?.name || 'SYSTEM'}</span>}
      </ImageButton>
    </div>
  );
}

Avatar.propTypes = {
  userId: PropTypes.number.isRequired,
  userProp: PropTypes.object,
  size: PropTypes.string,
  flip: PropTypes.bool,
  name: PropTypes.bool,
  className: PropTypes.string
};

Avatar.displayName = 'Avatar';

export default Avatar;
