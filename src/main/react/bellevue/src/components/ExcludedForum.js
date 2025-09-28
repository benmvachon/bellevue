import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getForum } from '../api/api.js';
import Button from './Button.js';

function ExcludedForum({ id, includeForum }) {
  const { pushAlert } = useOutletContext();
  const [forum, setForum] = useState(undefined);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (error) {
      pushAlert({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Error: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [pushAlert, error]);

  useEffect(() => {
    if (id) getForum(id, setForum, setError);
  }, [id]);

  const forumClick = (event) => {
    event.preventDefault();
    includeForum(id);
  };

  if (!forum) return;

  return (
    <div>
      <Button onClick={forumClick}>{forum.name}</Button>
    </div>
  );
}

ExcludedForum.propTypes = {
  id: PropTypes.number.isRequired,
  includeForum: PropTypes.func.isRequired
};

ExcludedForum.displayName = 'ExcludedForum';

export default ExcludedForum;
