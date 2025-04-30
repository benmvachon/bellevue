import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import { getFavorites } from '../api/api.js';
import InfiniteScroll from './InfiniteScroll.js';

function Favorites({ show = false, onClose }) {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (show) getFavorites(setFavorites, setError);
  }, [show]);

  const loadMore = (page) => {
    getFavorites(
      (more) => {
        if (more) {
          more.content = more?.content?.concat(favorites?.content);
          more.number = more.number + favorites?.number || 0;
          setFavorites(more);
        }
      },
      setError,
      page
    );
  };

  const navigateToFavorite = (favorite) => {
    navigate(`/${favorite.type.toLowerCase()}/${favorite.entity}`);
    onClose();
  };

  if (!show) return;
  if (error) return JSON.stringify(error);

  return (
    <div className="modal-container">
      <div className="modal favorites-container">
        <div className="favorites">
          <InfiniteScroll
            page={favorites}
            renderItem={(favorite) => (
              <div className="favorite" key={`favorite-${favorite.id}`}>
                <button onClick={() => navigateToFavorite(favorite)}>
                  {favorite.type} - {favorite.entity}
                </button>
              </div>
            )}
            loadMore={loadMore}
            reverse
          />
        </div>
        <div className="buttons">
          <button onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

Favorites.propTypes = {
  show: PropTypes.bool,
  friend: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired
};

export default withAuth(Favorites);
