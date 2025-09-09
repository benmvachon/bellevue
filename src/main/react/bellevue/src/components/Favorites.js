import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { getFavorites } from '../api/api.js';
import Page from './Page.js';
import Modal from './Modal.js';
import Favorite from './Favorite.js';

function Favorites({ show = false, onClose }) {
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
                <Favorite favorite={favorite} key={`favorite-${favorite.id}`} />
              )}
              loadPage={loadPage}
              reverse
            />
          ) : (
            <p>no favorites</p>
          )}
        </div>
      )}
      <div className="buttons">
        <button onClick={onClose}>close</button>
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
