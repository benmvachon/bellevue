import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getForum, getPost, getProfile } from '../api/api.js';

function Favorites({ favorite }) {
  const navigate = useNavigate();
  const [entity, setEntity] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    setLoading(true);
    let request = getProfile;
    switch (favorite.type.toLowerCase()) {
      case 'profile':
        request = getProfile;
        break;
      case 'forum':
        request = getForum;
        break;
      case 'post':
        request = getPost;
        break;
      default:
        break;
    }
    request(
      favorite.entity,
      (entity) => {
        setEntity(entity);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      }
    );
  }, [favorite]);

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
  };

  const renderFavorite = (favorite) => {
    switch (favorite.type.toLowerCase()) {
      case 'profile':
        return `${entity?.name}'s profile`;
      case 'forum':
        return `the ${entity?.name} building`;
      case 'post':
        return `flyer posted by ${entity?.user?.name} to the ${entity?.forum?.name} building`;
      default:
        return favorite.type.toLowerCase();
    }
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="favorite">
      {loading ? (
        <p>Loading...</p>
      ) : (
        <button onClick={() => navigateToFavorite(favorite)}>
          {renderFavorite(favorite)}
        </button>
      )}
    </div>
  );
}

Favorites.propTypes = {
  favorite: PropTypes.object.isRequired
};

Favorites.displayName = 'Favorites';

export default Favorites;
