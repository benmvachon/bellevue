import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { getForum } from '../api/api.js';

function ExcludedForum({ id, includeForum }) {
  const [forum, setForum] = useState(undefined);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (id) getForum(id, setForum, setError);
  }, [id]);

  const forumClick = (event) => {
    event.preventDefault();
    includeForum(id);
  };

  if (error) return JSON.stringify(error);
  if (!forum) return;

  return (
    <div>
      <button onClick={forumClick}>{forum.name}</button>
    </div>
  );
}

ExcludedForum.propTypes = {
  id: PropTypes.number.isRequired,
  includeForum: PropTypes.func.isRequired
};

ExcludedForum.displayName = 'ExcludedForum';

export default ExcludedForum;
