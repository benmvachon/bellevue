import React, { useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import FriendsMap from '../components/FriendsMap.js';
import ImageButton from '../components/ImageButton.js';
import Button from '../components/Button.js';

function FriendsMapPage() {
  const navigate = useNavigate();
  const { setClassName, setMapSlider } = useOutletContext();

  useEffect(() => {
    setClassName('map-page');
    setMapSlider(false);
  }, [setClassName, setMapSlider]);

  return (
    <div className="map-slider">
      <div className="content">
        <Button className="forth" onClick={() => navigate('/map/town')}>
          &lt;
        </Button>
        <FriendsMap />
        <Button className="forth" onClick={() => navigate('/map/suburbs')}>
          &gt;
        </Button>
        <div className="hide-map-slider">
          <ImageButton name="map-close" onClick={() => navigate('/')} />
        </div>
      </div>
    </div>
  );
}

FriendsMapPage.displayName = 'FriendsMapPage';

export default withAuth(FriendsMapPage);
