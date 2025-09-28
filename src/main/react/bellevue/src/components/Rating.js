import PropTypes from 'prop-types';
import Button from './Button';

function Rating({ rating = 0, ratingCount = 0, onClick }) {
  return (
    <div className="rating">
      <Button
        onClick={() => onClick('one')}
        className={`star ${rating >= 1 ? 'full' : 'empty'}`}
      />
      <Button
        onClick={() => onClick('two')}
        className={`star ${rating >= 2 ? 'full' : 'empty'}`}
      />
      <Button
        onClick={() => onClick('three')}
        className={`star ${rating >= 3 ? 'full' : 'empty'}`}
      />
      <Button
        onClick={() => onClick('four')}
        className={`star ${rating >= 4 ? 'full' : 'empty'}`}
      />
      <Button
        onClick={() => onClick('five')}
        className={`star ${rating >= 5 ? 'full' : 'empty'}`}
      />
      <span>({ratingCount})</span>
    </div>
  );
}

Rating.propTypes = {
  rating: PropTypes.number,
  ratingCount: PropTypes.number,
  onClick: PropTypes.func
};

Rating.displayName = 'Rating';

export default Rating;
