import React from 'react';
import PropTypes from 'prop-types';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import asPage from '../utils/asPage.js';
import ForumsMap from '../components/ForumsMap.js';
import FriendsMap from '../components/FriendsMap.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';

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
    <div className="page-contents">
      <h2>Map</h2>
      <div className="map">
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
      </div>
    </div>
  );
}

MapPage.propTypes = {
  setShowForumForm: PropTypes.func.isRequired
};

MapPage.displayName = 'MapPage';

export default withAuth(asPage(MapPage, 'map-page', false));
