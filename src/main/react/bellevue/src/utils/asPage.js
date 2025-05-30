import React from 'react';
import Header from '../components/Header.js';
import MapSlider from '../components/MapSlider.js';

const asPage = (WrappedComponent, className) => {
  const PageComponent = (props) => {
    return (
      <div key="main-page-container" className={`page ${className}`}>
        <Header key="page-header" />
        <MapSlider key="page-map-slider" />
        <WrappedComponent {...props} />
      </div>
    );
  };

  PageComponent.displayName = `asPage(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`;

  return PageComponent;
};

export default asPage;
