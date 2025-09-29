import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { addPost, addReply } from '../api/api';
import ForumTypeahead from './ForumTypeahead.js';
import Button from './Button.js';

const PostForm = ({ forum, parent, enableForumSelection = false }) => {
  const { pushAlert } = useOutletContext();
  const [newPost, setNewPost] = useState(null);
  const [selectedForum, setSelectedForum] = useState(forum);
  const [disabled, setDisabled] = useState(false);
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
    if (!newPost || !selectedForum) setDisabled(true);
    else setDisabled(false);
  }, [newPost, selectedForum]);

  const handleKeyDown = (e) => {
    if (e.shiftKey) return;
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

  return (
    <form onSubmit={submitPost}>
      <textarea
        name="newPost"
        placeholder={parent ? 'new reply...' : 'new flyer...'}
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
      <Button className="post-button" disabled={disabled} type="submit">
        {parent ? 'reply to flyer' : 'post new flyer'}
      </Button>
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
