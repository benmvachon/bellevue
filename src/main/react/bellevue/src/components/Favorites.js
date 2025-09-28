import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router';
import PropTypes from 'prop-types';
import { getFavorites } from '../api/api.js';
import Page from './Page.js';
import Modal from './Modal.js';
import Favorite from './Favorite.js';
import LoadingSpinner from './LoadingSpinner.js';
import Button from './Button.js';

function Favorites({ show = false, onClose, pushAlert }) {
  const outletContext = useOutletContext();
  const [favorites, setFavorites] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (error) {
      let func = pushAlert;
      if (outletContext) func = outletContext.pushAlert;
      func({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Error: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [outletContext, pushAlert, error]);

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

  return (
    <Modal className="favorites-container" show={show} onClose={onClose}>
      {loading ? (
        <div className="favorites">
          <LoadingSpinner onClick={() => {}} />
        </div>
      ) : (
        <div className="favorites">
          {favorites?.content?.length ? (
            <Page
              page={favorites}
              renderItem={(favorite) => (
                <Favorite
                  favorite={favorite}
                  key={`favorite-${favorite.id}`}
                  pushAlert={pushAlert}
                />
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
        <Button onClick={onClose}>close</Button>
      </div>
    </Modal>
  );
}

Favorites.propTypes = {
  show: PropTypes.bool,
  friend: PropTypes.number.isRequired,
  onClose: PropTypes.func.isRequired,
  pushAlert: PropTypes.func.isRequired
};

Favorites.displayName = 'Favorites';

export default Favorites;
