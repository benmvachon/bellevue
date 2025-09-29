import React, { useState, useEffect, useRef } from 'react';
import { useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import Button from './Button';

const Typeahead = ({
  onSelect,
  selectedValue,
  getDisplayValue,
  getDisplayOption,
  getOptions,
  className,
  name = 'typeahead',
  placeholder,
  multiselect = false,
  pushAlert
}) => {
  const outletContext = useOutletContext();
  const [query, setQuery] = useState('');
  const [options, setOptions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [error, setError] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);

  const inputRef = useRef();
  const dropdownRef = useRef();
  const timeoutRef = useRef();
  const itemRefs = useRef([]);

  useEffect(() => {
    if (error) {
      let func = pushAlert;
      if (outletContext) func = outletContext.pushAlert;
      func({
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
  }, [outletContext, pushAlert, error]);

  useEffect(() => {
    clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => {
      getOptions(
        query,
        (results) => {
          setOptions(results);
          setShowDropdown(document.activeElement === inputRef.current);
          setHighlightedIndex(-1);
        },
        setError
      );
    }, 300);
  }, [query, getOptions]);

  useEffect(() => {
    if (!multiselect && selectedValue) {
      clearTimeout(timeoutRef.current);
      setOptions([]);
      setShowDropdown(false);
      setHighlightedIndex(-1);
      return;
    }
  }, [multiselect, selectedValue]);

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

  useEffect(() => {
    query && onSelect(undefined);
  }, [query, onSelect]);

  const handleFocus = (event) => {
    event.preventDefault();
    if (!multiselect && selectedValue) {
      onSelect(undefined);
    }
    options.length > 0 && setShowDropdown(true);
  };

  const handleSelect = (forum) => {
    if (!multiselect) {
      setQuery('');
      setShowDropdown(false);
      setHighlightedIndex(-1);
    }
    onSelect(forum);
  };

  const handleKeyDown = (e) => {
    if (!showDropdown || options.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setHighlightedIndex((prev) =>
          prev < options.length - 1 ? prev + 1 : 0
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setHighlightedIndex((prev) =>
          prev > 0 ? prev - 1 : options.length - 1
        );
        break;
      case 'Enter':
        e.preventDefault();
        if (highlightedIndex >= 0 && highlightedIndex < options.length) {
          handleSelect(options[highlightedIndex]);
        }
        break;
      case 'Escape':
        setShowDropdown(false);
        break;
      default:
        break;
    }
  };

  return (
    <div className={`typeahead-container ${className}`}>
      {multiselect && (
        <div className="selected-options">{getDisplayValue(selectedValue)}</div>
      )}
      <input
        ref={inputRef}
        name={name}
        id={name}
        type="text"
        placeholder={placeholder}
        value={
          multiselect
            ? query
            : selectedValue
              ? getDisplayValue(selectedValue)
              : query
        }
        onChange={(e) => setQuery(e.target.value)}
        onFocus={handleFocus}
        onKeyDown={handleKeyDown}
        className="typeahead-input"
        autoComplete="off"
      />
      {showDropdown && options.length > 0 && (
        <ul className="typeahead-dropdown" ref={dropdownRef}>
          {options?.map((option, index) => (
            <li key={option.id} onMouseEnter={() => setHighlightedIndex(index)}>
              <Button
                ref={(el) => (itemRefs.current[index] = el)}
                onClick={() => handleSelect(option)}
                className={`typeahead-item${
                  index === highlightedIndex ? ' selected' : ''
                }`}
                onMouseDown={(e) => e.preventDefault()}
              >
                {getDisplayOption(option)}
              </Button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

Typeahead.propTypes = {
  onSelect: PropTypes.func.isRequired,
  selectedValue: PropTypes.any,
  getDisplayValue: PropTypes.func.isRequired,
  getDisplayOption: PropTypes.func.isRequired,
  getOptions: PropTypes.func.isRequired,
  className: PropTypes.string.isRequired,
  name: PropTypes.string,
  placeholder: PropTypes.string.isRequired,
  multiselect: PropTypes.bool,
  pushAlert: PropTypes.func
};

Typeahead.displayName = 'Typeahead';

export default Typeahead;
