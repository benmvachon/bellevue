import React, { useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import FriendsMap from '../components/FriendsMap.js';
import ImageButton from '../components/ImageButton.js';

function FriendsMapPage() {
  const navigate = useNavigate();
  const { setClassName, setMapSlider } = useOutletContext();

  useEffect(() => {
    setClassName('map-page');
    setMapSlider(false);
  });

  return (
    <div className="map-slider">
      <div className="content">
        <button className="forth" onClick={() => navigate('/map/town')}>
          &lt;
        </button>
        <FriendsMap />
        <button className="forth" onClick={() => navigate('/map/suburbs')}>
          &gt;
        </button>
        <div className="hide-map-slider">
          <ImageButton name="map-close" onClick={() => navigate('/')} />
        </div>
      </div>
    </div>
  );
}

FriendsMapPage.displayName = 'FriendsMapPage';

export default withAuth(FriendsMapPage);
