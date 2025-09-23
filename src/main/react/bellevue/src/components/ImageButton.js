import React from 'react';
import PropTypes from 'prop-types';
import Button from './Button';

function ImageButton({
  name,
  size = 'small',
  hat,
  icon,
  face = true,
  top = 0,
  left = 0,
  flip = false,
  className,
  onClick,
  children
}) {
  const style = { top, left };
  return (
    <Button
      className={`image-button button ${name} ${size} ${className}`}
      onClick={onClick}
    >
      {children}
      {name && (
        <img
          className={`image ${name} ${size}${flip ? ' flip' : ''}`}
          style={style}
          src={require(`../asset/${name}-${size}.png`)}
          alt={name}
        />
      )}
      {name && face && (
        <img
          className={`image face ${size}${flip ? ' flip' : ''}`}
          style={style}
          src={require(`../asset/face-${size}.png`)}
          alt={`${name}-face`}
        />
      )}
      {name && hat && (
        <img
          className={`image ${hat} ${size}${flip ? ' flip' : ''}`}
          style={style}
          src={require(`../asset/${hat}-${size}.png`)}
          alt={`${hat}`}
        />
      )}
      {name && icon && (
        <img
          className={`image ${icon} icon ${size}${flip ? ' flip' : ''}`}
          style={style}
          src={require(`../asset/${icon}-icon.png`)}
          alt={`${icon}`}
        />
      )}
    </Button>
  );
}

ImageButton.propTypes = {
  name: PropTypes.string.isRequired,
  size: PropTypes.string,
  hat: PropTypes.string,
  icon: PropTypes.string,
  face: PropTypes.bool,
  top: PropTypes.number,
  left: PropTypes.number,
  flip: PropTypes.bool,
  className: PropTypes.string,
  onClick: PropTypes.func,
  children: PropTypes.any
};

export default ImageButton;
