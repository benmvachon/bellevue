import React, { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import { queryForums } from '../api/api';

const ForumTypeahead = ({ onSelect, defaultForum }) => {
  const [query, setQuery] = useState('');
  const [forums, setForums] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [selectedForum, setSelectedForum] = useState(defaultForum);
  const [error, setError] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);

  const inputRef = useRef();
  const dropdownRef = useRef();
  const timeoutRef = useRef();
  const itemRefs = useRef([]);

  useEffect(() => {
    if (selectedForum) {
      setForums([]);
      setShowDropdown(false);
      setHighlightedIndex(-1);
      return;
    }

    clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => {
      queryForums(
        query,
        (results) => {
          setForums(results);
          setShowDropdown(true);
          setHighlightedIndex(-1);
        },
        setError
      );
    }, 300);
  }, [query, selectedForum]);

  // Auto-scroll to highlighted item
  useEffect(() => {
    if (highlightedIndex >= 0 && itemRefs.current[highlightedIndex]) {
      itemRefs.current[highlightedIndex].scrollIntoView({
        block: 'nearest',
        behavior: 'smooth'
      });
    }
  }, [highlightedIndex]);

  // Close dropdown on click outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (
        inputRef.current &&
        !inputRef.current.contains(e.target) &&
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target)
      ) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleFocus = (event) => {
    event.preventDefault();
    if (selectedForum) {
      onSelect && onSelect(undefined);
      setSelectedForum(undefined);
    }
    forums.length > 0 && setShowDropdown(true);
  };

  const handleSelect = (forum) => {
    setQuery('');
    setShowDropdown(false);
    setHighlightedIndex(-1);
    onSelect && onSelect(forum);
    setSelectedForum(forum);
  };

  const handleKeyDown = (e) => {
    if (!showDropdown || forums.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setHighlightedIndex((prev) =>
          prev < forums.length - 1 ? prev + 1 : 0
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setHighlightedIndex((prev) =>
          prev > 0 ? prev - 1 : forums.length - 1
        );
        break;
      case 'Enter':
        e.preventDefault();
        if (highlightedIndex >= 0 && highlightedIndex < forums.length) {
          handleSelect(forums[highlightedIndex]);
        }
        break;
      case 'Escape':
        setShowDropdown(false);
        break;
      default:
        break;
    }
  };

  if (error) return <pre>{JSON.stringify(error)}</pre>;

  return (
    <div className="forum-select">
      <input
        ref={inputRef}
        type="text"
        placeholder="Search forums..."
        value={selectedForum ? selectedForum.name : query}
        onChange={(e) => setQuery(e.target.value)}
        onFocus={handleFocus}
        onKeyDown={handleKeyDown}
        className="forum-select-input"
      />
      {showDropdown && forums.length > 0 && (
        <ul className="forum-select-dropdown" ref={dropdownRef}>
          {forums.map((forum, index) => (
            <li
              key={forum.id}
              ref={(el) => (itemRefs.current[index] = el)}
              onClick={() => handleSelect(forum)}
              className={`forum-select-item${
                index === highlightedIndex ? ' selected' : ''
              }`}
              onMouseDown={(e) => e.preventDefault()}
              onMouseEnter={() => setHighlightedIndex(index)}
            >
              <p className="name">{forum.name}</p>
              <p className="description">{forum.description}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

ForumTypeahead.propTypes = {
  onSelect: PropTypes.func.isRequired,
  defaultForum: PropTypes.object
};

export default ForumTypeahead;
