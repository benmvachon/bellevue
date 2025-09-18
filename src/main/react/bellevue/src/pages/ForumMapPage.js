import React, { useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import ForumsMap from '../components/ForumsMap.js';
import ImageButton from '../components/ImageButton.js';

function ForumMapPage({ setShowForumForm }) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();

  useEffect(() => {
    outletContext.setClassName('forum-map-page');
    outletContext.setMapSlider(false);
  }, [outletContext]);

  return (
    <div className="map-slider">
      <div className="content">
        <div className="custom-building-container">
          <ImageButton
            name="custom-building"
            onClick={() =>
              outletContext
                ? outletContext.setShowForumForm(true)
                : setShowForumForm(true)
            }
          >
            <span className="custom-button-label">New Building</span>
          </ImageButton>
        </div>
        <button className="back" disabled onClick={() => {}}>
          &lt;
        </button>
        <ForumsMap />
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

export default withAuth(ForumMapPage);
