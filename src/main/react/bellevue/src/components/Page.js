import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';

function Page({ page, renderItem, loadPage }) {
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
      {page?.content?.map(renderItem)}
      {hasPrevPage && (
        <button onClick={prevPage}>Previous page ({page.number})</button>
      )}
      {hasNextPage && (
        <button onClick={nextPage}>Next page ({page.number + 2})</button>
      )}
    </div>
  );
}

Page.propTypes = {
  page: PropTypes.object,
  renderItem: PropTypes.func.isRequired,
  loadPage: PropTypes.func.isRequired
};

export default withAuth(Page);
