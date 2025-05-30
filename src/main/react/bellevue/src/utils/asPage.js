import React from 'react';
import Header from '../components/Header.js';
import MapSlider from '../components/MapSlider.js';

const asPage = (WrappedComponent, className) => {
  const PageComponent = (props) => {
    return (
      <div className={`page ${className}`}>
        <Header />
        <MapSlider />
        <WrappedComponent {...props} />
      </div>
    );
  };

  PageComponent.displayName = `asPage(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`;

  return PageComponent;
};

export default asPage;
