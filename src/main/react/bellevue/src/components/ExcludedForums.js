import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import ExcludedForum from './ExcludedForum.js';

function ExcludedForums({ excludedForums, includeForum, clearFilter }) {
  if (!excludedForums || !excludedForums.length) return;
  return (
    <div>
      <p>Excluded forums:</p>
      {excludedForums.map((forum) => (
        <ExcludedForum
          key={`exclude-forum-${forum}`}
          id={forum}
          includeForum={includeForum}
        />
      ))}
      <button onClick={clearFilter}>Show all</button>
    </div>
  );
}

ExcludedForums.propTypes = {
  excludedForums: PropTypes.array.isRequired,
  includeForum: PropTypes.func.isRequired,
  clearFilter: PropTypes.func.isRequired
};

export default withAuth(ExcludedForums);
