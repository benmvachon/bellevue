import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { markMessageRead } from '../api/api.js';
import { formatContent } from '../utils/ContentFormatter.js';
import { formatTimeAgo } from '../utils/DateFormatter.js';

function Message({ friendId, id, message, sentOrReceived, read, date }) {
  const [hovering, setHovering] = useState(false);

  useEffect(() => {
    if ('received' === sentOrReceived && !read) markMessageRead(friendId, id);
  }, [friendId, id, read, sentOrReceived]);

  return (
    <div
      className="message-wrapper"
      onMouseOver={() => setHovering(true)}
      onMouseOut={() => setHovering(false)}
    >
      <div
        className={`message ${sentOrReceived}`}
        key={`message-${message.id}`}
        dangerouslySetInnerHTML={{
          __html: formatContent(message)
        }}
      />
      <div className={`tooltip  ${sentOrReceived} ${hovering}`}>
        {formatTimeAgo(date)}
      </div>
    </div>
  );
}

Message.propTypes = {
  friendId: PropTypes.number.isRequired,
  id: PropTypes.number.isRequired,
  message: PropTypes.string.isRequired,
  sentOrReceived: PropTypes.string.isRequired,
  read: PropTypes.bool.isRequired,
  date: PropTypes.object.isRequired
};

Message.displayName = 'Message';

export default Message;
