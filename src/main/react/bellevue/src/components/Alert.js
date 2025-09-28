import React, { useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import Button from './Button';

function Alert({ remove, className, type, content }) {
  const timeout = useRef(null);

  useEffect(() => {
    timeout.current = setTimeout(() => {
      remove();
    }, 100000);
    return () => clearTimeout(timeout.current);
  }, [remove]);

  const debounceClick = (event) => {
    if (event) {
      if (event.stopPropagation) event.stopPropagation();
      if (event.cancelable && !event.isDefaultPrevented())
        event.preventDefault();
    }
    remove && remove();
  };

  return (
    <div
      className={`alert pixel-corners ${type} ${className}`}
      onClick={debounceClick}
      onTouchStart={debounceClick}
    >
      <Button className="close" onClick={remove}>
        x
      </Button>
      {content}
    </div>
  );
}

Alert.propTypes = {
  remove: PropTypes.func.isRequired,
  type: PropTypes.string,
  className: PropTypes.string,
  content: PropTypes.any
};

export default Alert;
