import React from 'react';
import withAuth from '../utils/withAuth.js';
import asPage from '../utils/asPage.js';

function ErrorPage() {
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

export default withAuth(asPage(ErrorPage, 'error-page'));
