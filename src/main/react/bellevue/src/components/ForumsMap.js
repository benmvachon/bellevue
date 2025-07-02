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
      0
    );
  }, [filter, query]);

  useEffect(() => {
    if (
      forums &&
      forums.content &&
      !forums.content.find((forum) => forum.id === 1)
    ) {
      setLoading(true);
      getForum(
        1,
        (forum) => {
          forums.content.push(forum);
          setForums({ ...forums });
          setLoading(false);
        },
        setError
      );
    }
  }, [forums]);

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
      page
    );
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="forums">
      <h2>
        Town
        <button onClick={() => setFilter(!filter)}>
          {filter ? 'Show all' : 'Show unread'}
        </button>
        <input
          type="text"
          placeholder="Search town..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="forum-select-input"
        />
        <button onClick={markPostsRead}>Mark all as read</button>
        <button onClick={() => setShowForumForm(true)}>New Building</button>
      </h2>
      <p>Enter buildings and engage in conversations with your neighbors!</p>
      <div>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <Page
            page={forums}
            renderItem={(forum) => <Forum id={forum.id} forumProp={forum} />}
            loadPage={loadForumPage}
          />
        )}
      </div>
    </div>
  );
}

ForumsMap.propTypes = {
  setShowForumForm: PropTypes.func.isRequired
};

ForumsMap.displayName = 'ForumsMap';

export default ForumsMap;
