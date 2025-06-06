import React from 'react';
import PropTypes from 'prop-types';
import { getMyFriends } from '../api/api';
import Typeahead from './Typeahead';

const FriendTypeahead = ({ onSelect, onRemove, selectedFriends }) => {
  const getOptions = (query, callback, error) => {
    getMyFriends(
      (page) => callback(page?.content || []),
      error,
      query,
      selectedFriends.map((u) => u.id),
      0,
      10
    );
  };

  return (
    <Typeahead
      onSelect={onSelect}
      selectedValue={selectedFriends}
      getDisplayValue={(friends) =>
        friends.map((friend) => (
          <button key={`friend-${friend.id}`} onClick={() => onRemove(friend)}>
            {friend.name}
          </button>
        ))
      }
      getDisplayOption={(option) => (
        <p>
          <span className="name">{option.name}</span>
        </p>
      )}
      getOptions={getOptions}
      className="friend-select"
      placeholder="Search friends..."
      multiselect
    />
  );
};

FriendTypeahead.propTypes = {
  onSelect: PropTypes.func.isRequired,
  onRemove: PropTypes.func.isRequired,
  selectedFriends: PropTypes.array.isRequired
};

FriendTypeahead.displayName = 'FriendTypeahead';

export default FriendTypeahead;
