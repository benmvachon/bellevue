import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import PropTypes from 'prop-types';
import { addForum, updateForum } from '../api/api';
import FriendTypeahead from './FriendTypeahead.js';
import TagTypeahead from './TagTypeahead.js';
import Modal from './Modal.js';
import Forum from '../api/Forum.js';
import ImageButton from './ImageButton.js';

const ForumForm = ({ forum, show = false, onClose }) => {
  const navigate = useNavigate();
  const [name, setName] = useState(forum?.name || '');
  const [description, setDescription] = useState(forum?.description || '');
  const [tags, setTags] = useState(forum?.tags || []);
  const [users, setUsers] = useState(forum?.users || []);
  const [disabled, setDisabled] = useState(false);
  const [error, setError] = useState(false);

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
    if (!tag) return;
    if (!(tags.indexOf(tag) >= 0)) setTags(tags.concat([tag]));
  };

  const onTagRemove = (tag) => {
    if (!tag) return;
    setTags(tags.filter((t) => t !== tag));
  };

  const onFriendSelect = (friend) => {
    if (!friend) return;
    if (!(users.findIndex((user) => user.id === friend.id) >= 0))
      setUsers(users.concat([friend]));
  };

  const onFriendRemove = (friend) => {
    if (!friend) return;
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
        (forum) => {
          onCloseWrapper();
          navigate(`/town/${forum.id}`);
        },
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
        (forum) => {
          onCloseWrapper();
          navigate(`/town/${forum.id}`);
        },
        (error) => {
          setDisabled(false);
          setError(error);
        }
      );
    }
  };

  if (error) return <pre>{JSON.stringify(error)}</pre>;

  return (
    <Modal
      className="pixel-corners forum-form-container"
      show={show}
      onClose={onCloseWrapper}
    >
      <div className="custom-building-image-container">
        <ImageButton name="custom-building" size="medium" face={false}>
          <span>{forum?.name || 'New Building'}</span>
        </ImageButton>
      </div>
      <p>
        {forum
          ? 'Manage building details and control who can gain entry'
          : 'Construct a new building for you and your neighbors to hang out in'}
      </p>
      <form onSubmit={submit}>
        <label htmlFor="name">Name</label>
        <input
          name="name"
          id="name"
          autoComplete="off"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <label htmlFor="description">Description</label>
        <textarea
          name="description"
          id="description"
          autoComplete="off"
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <label htmlFor="tag-select">Tags</label>
        <TagTypeahead
          onSelect={onTagSelect}
          onRemove={onTagRemove}
          selectedTags={tags}
        />
        <label htmlFor="friend-select">Neighbors</label>
        <FriendTypeahead
          onSelect={onFriendSelect}
          onRemove={onFriendRemove}
          selectedFriends={users}
        />
        <div className="buttons">
          <button onClick={onCloseWrapper}>close</button>
          <button disabled={disabled} type="submit">
            {forum ? 'save' : 'create'}
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
