import PropTypes from 'prop-types';
import ExcludedForum from './ExcludedForum.js';

function ExcludedForums({ excludedForums, includeForum, clearFilter }) {
  if (!excludedForums || !excludedForums.length) return;
  return (
    <div className="excluded-forums-container">
      <p>Excluded forums:</p>
      <div className="excluded-forums">
        {excludedForums.map((forum) => (
          <ExcludedForum
            key={`exclude-forum-${forum}`}
            id={forum}
            includeForum={includeForum}
          />
        ))}
        <button onClick={clearFilter}>Show all</button>
      </div>
    </div>
  );
}

ExcludedForums.propTypes = {
  excludedForums: PropTypes.array.isRequired,
  includeForum: PropTypes.func.isRequired,
  clearFilter: PropTypes.func.isRequired
};

ExcludedForums.displayName = 'ExcludedForums';

export default ExcludedForums;
