import React, { useEffect } from 'react';
import { useNavigate, useOutletContext, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import FriendsOfFriendsMap from '../components/FriendsOfFriendsMap.js';
import ImageButton from '../components/ImageButton.js';

function FriendsOfFriendsMapPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { setClassName, setMapSlider } = useOutletContext();

  useEffect(() => {
    setClassName('map-page');
    setMapSlider(false);
  });

  return (
    <div className="map-slider">
      <div className="content">
        <FriendsOfFriendsMap id={id} />
        <div className="hide-map-slider">
          <ImageButton
            name="map-close"
            onClick={() => navigate(`/home/${id}`)}
          />
        </div>
      </div>
    </div>
  );
}

FriendsOfFriendsMapPage.displayName = 'FriendsOfFriendsMapPage';

export default withAuth(FriendsOfFriendsMapPage);
