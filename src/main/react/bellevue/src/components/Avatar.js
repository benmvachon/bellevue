import React, { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getProfile } from '../api/api.js';
import ImageButton from './ImageButton.js';

function Avatar({
  userId,
  userProp,
  size = 'small',
  flip = false,
  name = true,
  onClick,
  className,
  pushAlert
}) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const [user, setUser] = useState({ ...userProp });
  const [error, setError] = useState(false);

  useEffect(() => {
    if (error) {
      let func = pushAlert;
      if (outletContext) func = outletContext.pushAlert;
      func({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Error: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [outletContext, pushAlert, error]);

  useEffect(() => {
    if (userId < 1) {
      setUser({ id: 0, avatar: 'cat' });
    } else if (userProp) {
      setUser(userProp);
    } else {
      getProfile(userId, setUser, setError);
    }
  }, [userId, userProp, setUser]);

  const clickWrapper = () => {
    if (onClick) onClick();
    else user?.id > 0 && navigate(`/home/${user?.id}`);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className={`avatar ${className}${userId < 1 ? ' system' : ''}`}>
      <ImageButton
        name={user?.avatar}
        hat={user?.equipment?.hat}
        size={size}
        flip={flip}
        onClick={clickWrapper}
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
  onClick: PropTypes.func,
  className: PropTypes.string,
  pushAlert: PropTypes.func
};

Avatar.displayName = 'Avatar';

export default Avatar;
