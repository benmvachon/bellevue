import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';

function InfiniteScroll({ page, renderItem, loadMore, reverse = false }) {
  if (!page) return;
  const moreToLoad = page.totalPages > page.number + 1;
  const nextPage = () => {
    loadMore(page.number + 1);
  };
  if (reverse) {
    return (
      <div className="loader">
        {moreToLoad && <button onClick={nextPage}>Load more</button>}
        {page?.content?.map(renderItem)}
      </div>
    );
  }
  return (
    <div className="loader">
      {page?.content?.map(renderItem)}
      {moreToLoad && <button onClick={nextPage}>Load more</button>}
    </div>
  );
}

InfiniteScroll.propTypes = {
  page: PropTypes.object,
  renderItem: PropTypes.func.isRequired,
  loadMore: PropTypes.func.isRequired,
  reverse: PropTypes.bool
};

export default withAuth(InfiniteScroll);
