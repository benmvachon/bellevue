import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import {
  getForum,
  onForumUnreadUpdate,
  unsubscribeForumUnread
} from '../api/api.js';
import ImageButton from './ImageButton.js';

function Forum({ id, forumProp }) {
  const navigate = useNavigate();
  const [forum, setForum] = useState(forumProp);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (id && id !== forumProp?.id) {
      getForum(id, setForum, setError);
    } else if (forumProp) {
      setForum(forumProp);
    }
    onForumUnreadUpdate(id, () => getForum(id, setForum, setError));
    return () => unsubscribeForumUnread(id);
  }, [id, forumProp]);

  const forumClick = (event) => {
    event && event.preventDefault();
    navigate(`/town/${forum.id}`);
  };

  if (error) return JSON.stringify(error);
  if (!forum) return;
  let name = 'building';
  if (forum.id === 1) name = 'townhall';

  return (
    <ImageButton name={name} size="medium" face={false} onClick={forumClick}>
      <span className="forum-name">{forum.name}</span>
      <span className="forum-count">{forum.unreadCount} unread</span>
    </ImageButton>
  );
}

Forum.propTypes = {
  id: PropTypes.number,
  forumProp: PropTypes.object
};

Forum.displayName = 'Forum';

export default Forum;
