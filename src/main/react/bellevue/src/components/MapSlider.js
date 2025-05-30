import React, { useEffect, useRef, useState } from 'react';
import ForumsMap from '../components/ForumsMap.js';
import FriendsMap from '../components/FriendsMap.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';

function MapSlider() {
  const [open, setOpen] = useState(false);
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

  if (!open) {
    return (
      <button className="show-map-slider" onClick={() => setOpen(true)}>
        &gt;
      </button>
    );
  } else {
    return (
      <div ref={containerRef} className="map-slider">
        <ForumsMap />
        <FriendsMap />
        <SuggestedFriendsMap />
        <button className="hide-map-slider" onClick={() => setOpen(false)}>
          &lt;
        </button>
      </div>
    );
  }
}

MapSlider.displayName = 'MapSlider';

export default MapSlider;
