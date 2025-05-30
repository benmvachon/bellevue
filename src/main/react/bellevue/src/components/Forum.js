import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import PropTypes from 'prop-types';
import {
  getForum,
  markForumRead,
  onForumUnreadUpdate,
  unsubscribeForumUnread
} from '../api/api.js';

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
    event.preventDefault();
    navigate(`/forum/${forum.id}`);
  };

  const markRead = () => {
    markForumRead(
      forum.id,
      () => getForum(forum.id, setForum, setError),
      setError
    );
  };

  if (error) return JSON.stringify(error);
  if (!forum) return;

  return (
    <div>
      <button onClick={forumClick}>
        {forum.name} - ({forum.unreadCount} unread)
      </button>
      {forum.unreadCount > 0 && (
        <button onClick={markRead}>Mark as read</button>
      )}
    </div>
  );
}

Forum.propTypes = {
  id: PropTypes.number,
  forumProp: PropTypes.object
};

Forum.displayName = 'Forum';

export default Forum;
