import PropTypes from 'prop-types';
import ImageButton from './ImageButton.js';

function FavoriteButton({ favorited = false, onClick }) {
  return (
    <ImageButton name="notepad" onClick={onClick} className="favorite-button">
      <span>{favorited ? 'remove from notepad' : 'add to notepad'}</span>
    </ImageButton>
  );
}

FavoriteButton.propTypes = {
  favorited: PropTypes.bool,
  onClick: PropTypes.func.isRequired
};

FavoriteButton.displayName = 'FavoriteButton';

export default FavoriteButton;
