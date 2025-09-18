import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import PropTypes from 'prop-types';
import ForumsMap from '../components/ForumsMap.js';
import FriendsMap from '../components/FriendsMap.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';
import ImageButton from './ImageButton.js';

function MapSlider({ setShowForumForm, pushAlert }) {
  const [open, setOpen] = useState(false);
  const [index, setIndex] = useState(0);
  const params = useParams();

  useEffect(() => {
    setOpen(false);
  }, [params, params.id]);

  const updateIndex = (index) => {
    if (index <= 2 && index >= 0) setIndex(index);
  };

  if (!open) {
    return (
      <div className="show-map-slider">
        <ImageButton name="map-open" onClick={() => setOpen(true)} />
      </div>
    );
  } else {
    return (
      <div className="map-slider">
        <div className="content">
          {index === 0 && (
            <div className="custom-building-container">
              <ImageButton
                name="custom-building"
                onClick={() => setShowForumForm(true)}
              >
                <span className="custom-button-label">New Building</span>
              </ImageButton>
            </div>
          )}
          <button
            className="back"
            disabled={index <= 0}
            onClick={() => updateIndex(index - 1)}
          >
            &lt;
          </button>
          {index === 0 && <ForumsMap pushAlert={pushAlert} />}
          {index === 1 && <FriendsMap pushAlert={pushAlert} />}
          {index === 2 && <SuggestedFriendsMap pushAlert={pushAlert} />}
          <button
            className="forth"
            disabled={index >= 2}
            onClick={() => updateIndex(index + 1)}
          >
            &gt;
          </button>
          <div className="hide-map-slider">
            <ImageButton name="map-close" onClick={() => setOpen(false)} />
          </div>
        </div>
      </div>
    );
  }
}

MapSlider.displayName = 'MapSlider';

MapSlider.propTypes = {
  setShowForumForm: PropTypes.func.isRequired,
  pushAlert: PropTypes.func
};

export default MapSlider;
