import PropTypes from 'prop-types';
import ImageButton from './ImageButton.js';

function LoadingSpinner({ onClick, size = 'small' }) {
  return (
    <div className="loading">
      <ImageButton
        name="spinner"
        size={size}
        face={false}
        onClick={onClick}
        className="loading-spinner"
      >
        <span>loading...</span>
      </ImageButton>
    </div>
  );
}

LoadingSpinner.propTypes = {
  onClick: PropTypes.func.isRequired,
  size: PropTypes.string
};

LoadingSpinner.displayName = 'LoadingSpinner';

export default LoadingSpinner;
