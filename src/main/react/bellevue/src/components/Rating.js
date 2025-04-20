import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';

function Rating({ rating = 0, ratingCount = 0, onClick }) {
  return (
    <div className="rating">
      <button
        onClick={() => onClick('one')}
        className={`star ${rating >= 1 ? 'full' : 'empty'}`}
      />
      <button
        onClick={() => onClick('two')}
        className={`star ${rating >= 2 ? 'full' : 'empty'}`}
      />
      <button
        onClick={() => onClick('three')}
        className={`star ${rating >= 3 ? 'full' : 'empty'}`}
      />
      <button
        onClick={() => onClick('four')}
        className={`star ${rating >= 4 ? 'full' : 'empty'}`}
      />
      <button
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

export default withAuth(Rating);
