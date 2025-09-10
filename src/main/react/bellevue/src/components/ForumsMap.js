import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { getForum, getForums, markPostsRead } from '../api/api.js';
import Page from '../components/Page.js';
import Forum from '../components/Forum.js';

function ForumsMap({ setShowForumForm }) {
  const [forums, setForums] = useState(undefined);
  const [filter, setFilter] = useState(false);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    setLoading(true);
    getForums(
      (forums) => {
        setForums(forums);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      filter,
      query,
      0,
      8
    );
  }, [filter, query]);

  useEffect(() => {
    if (forums && !forums.content?.find((forum) => forum && forum.id === 1)) {
      setLoading(true);
      getForum(
        1,
        (forum) => {
          for (let i = 8; i > 4; i--) {
            if (forums?.content) forums.content[i] = forums?.content[i - 1];
          }
          if (forums?.content) forums.content[4] = forum;
          setForums({ ...forums });
          setLoading(false);
        },
        setError
      );
    }
  }, [forums, forums?.content]);

  const loadForumPage = (page) => {
    setLoading(true);
    getForums(
      (forums) => {
        setForums(forums);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      filter,
      query,
      page,
      8
    );
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="forums">
      <div className="header pixel-corners">
        <h2>Town</h2>
        <button onClick={() => setFilter(!filter)}>
          {filter ? 'show all' : 'show unread'}
        </button>
        <input
          type="text"
          placeholder="Search town..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="forum-select-input"
        />
        <button onClick={markPostsRead}>mark all read</button>
      </div>
      <p className="pixel-corners">
        Enter buildings and engage in conversations with your neighbors
      </p>
      <div className="grid">
        <Page
          page={forums}
          renderItem={(forum) =>
            forum && <Forum id={forum.id} forumProp={forum} />
          }
          loadPage={loadForumPage}
          loading={loading}
        />
      </div>
    </div>
  );
}

ForumsMap.propTypes = {
  setShowForumForm: PropTypes.func.isRequired
};

ForumsMap.displayName = 'ForumsMap';

export default ForumsMap;
