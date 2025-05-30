import React from 'react';
import withAuth from '../utils/withAuth.js';
import Header from '../components/Header.js';
import ForumsMap from '../components/ForumsMap.js';
import FriendsMap from '../components/FriendsMap.js';
import SuggestedFriendsMap from '../components/SuggestedFriendsMap.js';

function HomePage() {
  return (
    <div className="page home-page">
      <Header />
      <div className="page-contents">
        <ForumsMap />
        <FriendsMap />
        <SuggestedFriendsMap />
      </div>
    </div>
  );
}

HomePage.displayName = 'HomePage';

export default withAuth(HomePage);
