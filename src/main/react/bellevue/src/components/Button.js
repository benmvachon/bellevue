import React, { useRef, useState } from 'react';
import PropTypes from 'prop-types';

function Button({ onClick, className, type, disabled, title, children }) {
  const [wait, setWait] = useState(false);
  const clickTimeout = useRef(null);

  const debounceClick = (event) => {
    if (event) {
      if (event.stopPropagation) event.stopPropagation();
      if (event.cancelable && !event.isDefaultPrevented())
        event.preventDefault();
    }
    if (wait) return;
    setWait(true);
    onClick && onClick();

    if (clickTimeout.current) {
      clearTimeout(clickTimeout.current);
    }

    clickTimeout.current = setTimeout(() => {
      setWait(false);
    }, 300);
  };

  return (
    <button
      className={`${className}`}
      type={type}
      onClick={debounceClick}
      onTouchStart={debounceClick}
      disabled={disabled}
      title={title}
    >
      {children}
    </button>
  );
}

Button.propTypes = {
  onClick: PropTypes.func,
  type: PropTypes.string,
  className: PropTypes.string,
  disabled: PropTypes.bool,
  title: PropTypes.string,
  children: PropTypes.any
};

export default Button;
