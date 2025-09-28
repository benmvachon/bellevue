import PropTypes from 'prop-types';
import ExcludedForum from './ExcludedForum.js';
import Button from './Button.js';

function ExcludedForums({ excludedForums, includeForum, clearFilter }) {
  return (
    <div className="excluded-forums-container">
      <p>filtered buildings:</p>
      <div className="excluded-forums">
        {excludedForums &&
          excludedForums.map((forum) => (
            <ExcludedForum
              key={`exclude-forum-${forum}`}
              id={forum}
              includeForum={includeForum}
            />
          ))}
        <Button
          onClick={clearFilter}
          disabled={!(excludedForums && excludedForums.length)}
        >
          show all
        </Button>
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
