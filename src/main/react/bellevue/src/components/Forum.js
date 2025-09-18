import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import {
  getForum,
  onForumUnreadUpdate,
  unsubscribeForumUnread
} from '../api/api.js';
import ImageButton from './ImageButton.js';

function Forum({ id, forumProp, pushAlert }) {
  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const [forum, setForum] = useState(forumProp);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (error) {
      let func = pushAlert;
      if (outletContext) func = outletContext.pushAlert;
      func({
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
  }, [outletContext, pushAlert, error]);

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

  if (!forum) return;

  let icon = undefined;
  let name = 'townhall';
  if (forum?.user?.id) {
    name = 'custom-building';
  } else if (forum.id > 1) {
    icon = forum.name.toLowerCase().replaceAll(' ', '-');
    name = 'building';
  }

  return (
    <ImageButton
      name={name}
      size="medium"
      face={false}
      icon={icon}
      onClick={forumClick}
    >
      <span className="forum-name">{forum.name}</span>
      <span className="forum-count">{forum.unreadCount} unread</span>
    </ImageButton>
  );
}

Forum.propTypes = {
  id: PropTypes.number,
  forumProp: PropTypes.object,
  pushAlert: PropTypes.func
};

Forum.displayName = 'Forum';

export default Forum;
