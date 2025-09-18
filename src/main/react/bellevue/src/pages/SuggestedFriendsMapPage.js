import React, { useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';
import ImageButton from '../components/ImageButton.js';

function SuggestedFriendsMapPage() {
  const navigate = useNavigate();
  const { setClassName, setMapSlider } = useOutletContext();

  useEffect(() => {
    setClassName('map-page');
    setMapSlider(false);
  }, [setClassName, setMapSlider]);

  return (
    <div className="map-slider">
      <div className="content">
        <button className="forth" onClick={() => navigate('/map/neighborhood')}>
          &lt;
        </button>
        <SuggestedFriendsMap />
        <button className="forth" disabled onClick={() => {}}>
          &gt;
        </button>
        <div className="hide-map-slider">
          <ImageButton name="map-close" onClick={() => navigate('/')} />
        </div>
      </div>
    </div>
  );
}

SuggestedFriendsMapPage.displayName = 'SuggestedFriendsMapPage';

SuggestedFriendsMapPage.propTypes = {};

export default withAuth(SuggestedFriendsMapPage);
