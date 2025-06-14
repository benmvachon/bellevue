import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { addForum, updateForum } from '../api/api';
import FriendTypeahead from './FriendTypeahead.js';
import TagTypeahead from './TagTypeahead.js';
import Modal from './Modal.js';
import Forum from '../api/Forum.js';

const ForumForm = ({ forum, show = false, onClose }) => {
  const [name, setName] = useState(forum?.name || '');
  const [description, setDescription] = useState(forum?.description || '');
  const [tags, setTags] = useState(forum?.tags || []);
  const [users, setUsers] = useState(forum?.users || []);
  const [disabled, setDisabled] = useState(false);
  const [error, setError] = useState(false);

  const onCloseWrapper = (event) => {
    event && event.preventDefault && event.preventDefault();
    setDisabled(false);
    setName('');
    setDescription('');
    setTags([]);
    setUsers([]);
    setError(false);
    onClose();
  };

  const onTagSelect = (tag) => {
    if (!(tags.indexOf(tag) >= 0)) setTags(tags.concat([tag]));
  };

  const onTagRemove = (tag) => {
    setTags(tags.filter((t) => t !== tag));
  };

  const onFriendSelect = (friend) => {
    if (!(users.findIndex((user) => user.id === friend.id) >= 0))
      setUsers(users.concat([friend]));
  };

  const onFriendRemove = (friend) => {
    setUsers(users.filter((user) => user.id !== friend.id));
  };

  const submit = (event) => {
    event.preventDefault();
    setDisabled(true);
    if (forum) {
      updateForum(
        forum.id,
        name,
        description,
        tags,
        users.map((user) => user.id),
        onCloseWrapper,
        (error) => {
          setDisabled(false);
          setError(error);
        }
      );
    } else {
      addForum(
        name,
        description,
        tags,
        users.map((user) => user.id),
        onCloseWrapper,
        (error) => {
          setDisabled(false);
          setError(error);
        }
      );
    }
  };

  useEffect(() => {
    if (show && forum) {
      setDisabled(false);
      setName(forum.name);
      setDescription(forum.description);
      setTags(forum.tags);
      setUsers(forum.users);
      setError(false);
    }
  }, [show, forum]);

  if (error) return <pre>{JSON.stringify(error)}</pre>;
  if (!show) return;

  return (
    <Modal
      className="forum-form-container"
      show={show}
      onClose={onCloseWrapper}
    >
      <h2>{forum ? 'Edit Forum' : 'New Forum'}</h2>
      <form onSubmit={submit}>
        <label>Name</label>
        <input
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <label>Description</label>
        <textarea
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <label>Tags</label>
        <TagTypeahead
          onSelect={onTagSelect}
          onRemove={onTagRemove}
          selectedTags={tags}
        />
        <label>Users</label>
        <FriendTypeahead
          onSelect={onFriendSelect}
          onRemove={onFriendRemove}
          selectedFriends={users}
        />
        <div className="buttons">
          <button onClick={onCloseWrapper}>Close</button>
          <button disabled={disabled} type="submit">
            {forum ? 'Save' : 'Create'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

ForumForm.propTypes = {
  forum: PropTypes.object.of(Forum),
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired
};

ForumForm.displayName = 'ForumForm';

export default ForumForm;
