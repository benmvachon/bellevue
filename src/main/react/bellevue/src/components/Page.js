import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

function Page({ page, renderItem, loadPage, loading = false }) {
  if (!page) return;
  const hasPrevPage = page.number > 0;
  const hasNextPage = page.totalPages > page.number + 1;
  const prevPage = () => {
    loadPage(page.number - 1);
  };
  const nextPage = () => {
    loadPage(page.number + 1);
  };
  return (
    <div className="loader">
      {loading ? (
        <LoadingSpinner onClick={() => {}} />
      ) : (
        <div className="page-content">{page?.content?.map(renderItem)}</div>
      )}
      <div className="pagination-buttons">
        <button onClick={prevPage} disabled={!hasPrevPage}>
          previous page ({page.number})
        </button>
        <button onClick={nextPage} disabled={!hasNextPage}>
          next page ({page.number + 2})
        </button>
      </div>
    </div>
  );
}

Page.propTypes = {
  page: PropTypes.object,
  renderItem: PropTypes.func.isRequired,
  loadPage: PropTypes.func.isRequired,
  loading: PropTypes.bool
};

Page.displayName = 'Page';

export default Page;
