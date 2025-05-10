import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';

function ScrollLoader({
  children,
  loadMore,
  topLoad = false,
  total = 0,
  className
}) {
  return (
    <div className={`scroll-loader ${className}`}>
      {topLoad && total > (children ? children.length : 0) && (
        <button onClick={loadMore}>Load more</button>
      )}
      {children}
      {!topLoad && total > (children ? children.length : 0) && (
        <button onClick={loadMore}>Load more</button>
      )}
    </div>
  );
}

ScrollLoader.propTypes = {
  children: PropTypes.element,
  loadMore: PropTypes.func,
  topLoad: PropTypes.bool,
  total: PropTypes.number,
  className: PropTypes.string
};

export default withAuth(ScrollLoader);
