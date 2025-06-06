import React, { useState } from 'react';
import Header from '../components/Header.js';
import MapSlider from '../components/MapSlider.js';
import ForumForm from '../components/ForumForm.js';

const asPage = (WrappedComponent, className) => {
  const PageComponent = (props) => {
    const [showForumForm, setShowForumForm] = useState(false);
    return (
      <div key="main-page-container" className={`page ${className}`}>
        <Header key="page-header" />
        <MapSlider key="page-map-slider" setShowForumForm={setShowForumForm} />
        <WrappedComponent {...props} />
        <ForumForm
          show={showForumForm}
          onClose={() => setShowForumForm(false)}
        />
      </div>
    );
  };

  PageComponent.displayName = `asPage(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`;

  return PageComponent;
};

export default asPage;
