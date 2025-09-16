import React from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import asPage from '../utils/asPage.js';
import ForumsMap from '../components/ForumsMap.js';
import ImageButton from '../components/ImageButton.js';

function ForumMapPage({ setShowForumForm }) {
  const navigate = useNavigate();

  return (
    <div className="map-slider">
      <div className="content">
        <div className="custom-building-container">
          <ImageButton
            name="custom-building"
            onClick={() => setShowForumForm(true)}
          >
            <span className="custom-button-label">New Building</span>
          </ImageButton>
        </div>
        <button className="back" disabled onClick={() => {}}>
          &lt;
        </button>
        <ForumsMap setShowForumForm={setShowForumForm} />
        <button className="forth" onClick={() => navigate('/map/neighborhood')}>
          &gt;
        </button>
        <div className="hide-map-slider">
          <ImageButton name="map-close" onClick={() => navigate('/')} />
        </div>
      </div>
    </div>
  );
}

ForumMapPage.displayName = 'ForumMapPage';

ForumMapPage.propTypes = {
  setShowForumForm: PropTypes.func.isRequired
};

export default withAuth(asPage(ForumMapPage, 'forum-map-page', false));
