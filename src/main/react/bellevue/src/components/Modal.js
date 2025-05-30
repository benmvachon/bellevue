import { useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';

function Modal({ show = false, onClose, children, className }) {
  const containerRef = useRef();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        onClose();
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [onClose]);

  if (!show) return;

  return (
    <div className="modal-container">
      <div ref={containerRef} className={`modal ${className}`}>
        <button className="modal-close" onClick={onClose}>
          X
        </button>
        {children}
      </div>
    </div>
  );
}

Modal.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  children: PropTypes.element,
  className: PropTypes.string
};

Modal.displayName = 'Modal';

export default withAuth(Modal);
