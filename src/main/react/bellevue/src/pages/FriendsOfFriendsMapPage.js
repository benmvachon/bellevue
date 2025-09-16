import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import asPage from '../utils/asPage.js';
import FriendsOfFriendsMap from '../components/FriendsOfFriendsMap.js';
import ImageButton from '../components/ImageButton.js';

function FriendsOfFriendsMapPage() {
  const { id } = useParams();
  const navigate = useNavigate();

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

export default withAuth(asPage(FriendsOfFriendsMapPage, 'map-page', false));
