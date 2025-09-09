import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import asPage from '../utils/asPage.js';
import ForumsMap from '../components/ForumsMap.js';
import FriendsMap from '../components/FriendsMap.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';
import ImageButton from '../components/ImageButton.js';

function MapPage({ setShowForumForm }) {
  const navigate = useNavigate();
  const { section } = useParams();
  let index = 0;
  if (section === 'neighborhood') index = 1;
  if (section === 'suburbs') index = 2;

  const updateIndex = (index) => {
    let newSection = 'forum';
    if (index === 1) newSection = 'neighborhood';
    if (index === 2) newSection = 'suburbs';
    navigate(`/map/${newSection}`);
  };
  return (
    <div className="map-slider">
      <div className="content">
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
        <div className="hide-map-slider">
          <ImageButton name="map-close" onClick={() => navigate('/')} />
        </div>
      </div>
    </div>
  );
}

MapPage.displayName = 'MapSlider';

MapPage.propTypes = {
  setShowForumForm: PropTypes.func.isRequired
};

export default withAuth(asPage(MapPage, 'map-page', false));
