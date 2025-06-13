import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getFavorites } from '../api/api.js';
import Page from './Page.js';
import Modal from './Modal.js';

function Favorites({ show = false, onClose }) {
  const navigate = useNavigate();
  const [favorites, setFavorites] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (show) {
      setLoading(true);
      getFavorites(
        (favorites) => {
          setFavorites(favorites);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        }
      );
    }
  }, [show]);

  const loadPage = (page) => {
    getFavorites(setFavorites, setError, page);
  };

  const getType = (type) => {
    switch (type.toLowerCase()) {
      case 'profile':
        return 'home';
      case 'forum':
        return 'town';
      case 'post':
        return 'flyer';
      default:
        return type.toLowerCase();
    }
  };

  const navigateToFavorite = (favorite) => {
    navigate(`/${getType(favorite.type)}/${favorite.entity}`);
    onClose();
  };

  if (!show) return;
  if (error) return JSON.stringify(error);

  return (
    <Modal className="favorites-container" show={show} onClose={onClose}>
      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="favorites">
          {favorites?.content?.length ? (
            <Page
              page={favorites}
              renderItem={(favorite) => (
                <div className="favorite" key={`favorite-${favorite.id}`}>
                  <button onClick={() => navigateToFavorite(favorite)}>
                    {favorite.type} - {favorite.entity}
                  </button>
                </div>
              )}
              loadPage={loadPage}
              reverse
            />
          ) : (
            <p>No favorites</p>
          )}
        </div>
      )}
      <div className="buttons">
        <button onClick={onClose}>Close</button>
      </div>
    </Modal>
  );
}

Favorites.propTypes = {
  show: PropTypes.bool,
  friend: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired
};

Favorites.displayName = 'Favorites';

export default Favorites;
