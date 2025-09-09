import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { addPost, addReply } from '../api/api';
import ForumTypeahead from './ForumTypeahead.js';

const PostForm = ({ forum, parent, enableForumSelection = false }) => {
  const [newPost, setNewPost] = useState(null);
  const [selectedForum, setSelectedForum] = useState(forum);
  const [disabled, setDisabled] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!newPost || !selectedForum) setDisabled(true);
    else setDisabled(false);
  }, [newPost, selectedForum]);

  const handleKeyDown = (e) => {
    switch (e.key) {
      case 'Enter':
        if (newPost && selectedForum) submitPost(e);
        break;
      default:
        break;
    }
  };

  const submitPost = (event) => {
    event.preventDefault();
    if (parent)
      addReply(
        selectedForum.id,
        parent.id,
        newPost,
        () => setNewPost(''),
        setError
      );
    else addPost(selectedForum.id, newPost, () => setNewPost(''), setError);
  };

  if (error) return <pre>{JSON.stringify(error)}</pre>;

  return (
    <form onSubmit={submitPost}>
      <textarea
        name="newPost"
        value={newPost}
        onChange={(e) => setNewPost(e.target.value)}
        onKeyDown={handleKeyDown}
      />
      {enableForumSelection && (
        <ForumTypeahead
          onSelect={setSelectedForum}
          selectedForum={selectedForum}
        />
      )}
      <button disabled={disabled} type="submit">
        {parent ? 'reply to flyer' : 'post new flyer'}
      </button>
    </form>
  );
};

PostForm.propTypes = {
  forum: PropTypes.object,
  parent: PropTypes.object,
  enableForumSelection: PropTypes.bool
};

PostForm.displayName = 'PostForm';

export default PostForm;
