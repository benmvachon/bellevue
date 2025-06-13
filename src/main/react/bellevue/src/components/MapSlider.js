import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import ForumsMap from '../components/ForumsMap.js';
import FriendsMap from '../components/FriendsMap.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';

function MapSlider({ setShowForumForm }) {
  const [open, setOpen] = useState(false);
  const [index, setIndex] = useState(0);
  const containerRef = useRef();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const updateIndex = (index) => {
    if (index <= 2 && index >= 0) setIndex(index);
  };

  if (!open) {
    return (
      <button className="show-map-slider" onClick={() => setOpen(true)}>
        &gt;
      </button>
    );
  } else {
    return (
      <div ref={containerRef} className="map-slider">
        <button
          className="back"
          disabled={index <= 0}
          onClick={() => updateIndex(index - 1)}
        >
          &lt;
        </button>
        {index === 0 && <ForumsMap setShowForumForm={setShowForumForm} />}
        {index === 1 && <FriendsMap />}
        {index === 2 && <SuggestedFriendsMap />}
        <button
          className="forth"
          disabled={index >= 2}
          onClick={() => updateIndex(index + 1)}
        >
          &gt;
        </button>
        <button className="hide-map-slider" onClick={() => setOpen(false)}>
          &lt;
        </button>
      </div>
    );
  }
}

MapSlider.displayName = 'MapSlider';

MapSlider.propTypes = {
  setShowForumForm: PropTypes.func.isRequired
};

export default MapSlider;
