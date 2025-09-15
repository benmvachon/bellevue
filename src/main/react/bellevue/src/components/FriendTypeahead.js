import React from 'react';
import PropTypes from 'prop-types';
import { getMyFriends } from '../api/api';
import Typeahead from './Typeahead';
import Avatar from './Avatar';

const FriendTypeahead = ({ onSelect, onRemove, selectedFriends }) => {
  const getOptions = (query, callback, error) => {
    getMyFriends(
      (page) => callback(page?.content || []),
      error,
      query,
      selectedFriends?.map((u) => u.id) || [],
      0,
      10
    );
  };

  return (
    <Typeahead
      onSelect={onSelect}
      selectedValue={selectedFriends}
      getDisplayValue={(friends) =>
        friends?.map(
          (friend) =>
            friend && (
              <Avatar
                key={`avatar-${friend?.id}`}
                userId={friend?.id}
                userProp={friend}
                name={true}
                onClick={() => onRemove(friend)}
              />
            )
        )
      }
      getDisplayOption={(option) => (
        <p>
          <span className="name">{option.name}</span>
          <span className="description">{option.username}</span>
        </p>
      )}
      getOptions={getOptions}
      className="friend-select"
      name="friend-select"
      placeholder="Search neighbors..."
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
