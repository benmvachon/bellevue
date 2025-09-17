import React, { useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';

function ErrorPage() {
  const { setClassName, setMapSlider } = useOutletContext();

  useEffect(() => {
    setClassName('error-page');
    setMapSlider(true);
  });

  return (
    <div className="page-contents">
      <h1>ERROR</h1>
      <p>Sorry!</p>
      <p>
        There was an error and you were brought to page which does not exist
      </p>
    </div>
  );
}

ErrorPage.displayName = 'ErrorPage';

export default withAuth(ErrorPage);
