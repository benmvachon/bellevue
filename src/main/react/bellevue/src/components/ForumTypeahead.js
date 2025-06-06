import React from 'react';
import PropTypes from 'prop-types';
import { getForums } from '../api/api';
import Typeahead from './Typeahead';

const ForumTypeahead = ({ onSelect, selectedForum }) => {
  const getOptions = (query, callback, error) => {
    getForums(
      (page) => callback(page?.content || []),
      error,
      false,
      query,
      0,
      10
    );
  };

  return (
    <Typeahead
      onSelect={onSelect}
      selectedValue={selectedForum}
      getDisplayValue={(forum) => forum.name}
      getDisplayOption={(option) => (
        <p>
          <span className="name">{option.name}</span>
          <span className="description">{option.description}</span>
        </p>
      )}
      getOptions={getOptions}
      className="forum-select"
      placeholder="Search forums..."
    />
  );
};

ForumTypeahead.propTypes = {
  onSelect: PropTypes.func.isRequired,
  selectedForum: PropTypes.object.isRequired
};

ForumTypeahead.displayName = 'ForumTypeahead';

export default ForumTypeahead;
