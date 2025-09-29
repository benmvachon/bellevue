import React, { useRef, useState } from 'react';
import PropTypes from 'prop-types';

function Button({
  onClick,
  className,
  type,
  disabled,
  title,
  dangerouslySetInnerHTML,
  children
}) {
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
    }, 100);
  };

  return (
    <button
      className={`${className}`}
      type={type}
      onClick={onClick ? debounceClick : undefined}
      onTouchEnd={onClick ? debounceClick : undefined}
      onTouchStart={onClick ? (e) => e.preventDefault() : undefined}
      onMouseDown={onClick ? (e) => e.preventDefault() : undefined}
      onMouseUp={onClick ? (e) => e.preventDefault() : undefined}
      disabled={disabled}
      title={title}
      dangerouslySetInnerHTML={dangerouslySetInnerHTML}
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
  dangerouslySetInnerHTML: PropTypes.any,
  children: PropTypes.any
};

export default Button;
