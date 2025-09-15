import React from 'react';
import PropTypes from 'prop-types';
import { getTags } from '../api/api';
import Typeahead from './Typeahead';

const TagTypeahead = ({ onSelect, onRemove, selectedTags }) => {
  const getOptions = (query, callback, error) => {
    if (!query) callback([]);
    else {
      getTags(
        (page) => {
          if (page && page.content) callback([query].concat(page.content));
          else callback([query]);
        },
        error,
        query,
        0,
        10
      );
    }
  };

  return (
    <Typeahead
      onSelect={onSelect}
      selectedValue={selectedTags}
      getDisplayValue={(tags) =>
        tags?.map(
          (tag) =>
            tag && (
              <button key={`tag-${tag}`} onClick={() => onRemove(tag)}>
                {tag}
              </button>
            )
        )
      }
      getDisplayOption={(option) => (
        <p>
          <span className="name">{option}</span>
        </p>
      )}
      getOptions={getOptions}
      className="tag-select"
      name="tag-select"
      placeholder="Search tags..."
      multiselect
    />
  );
};

TagTypeahead.propTypes = {
  onSelect: PropTypes.func.isRequired,
  onRemove: PropTypes.func.isRequired,
  selectedTags: PropTypes.array.isRequired
};

TagTypeahead.displayName = 'TagTypeahead';

export default TagTypeahead;
