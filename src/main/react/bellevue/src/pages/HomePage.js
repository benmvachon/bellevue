import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getCategories } from '../api/api.js';
import Header from '../components/Header.js';
import InfiniteScroll from '../components/InfiniteScroll.js';

function HomePage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    getCategories(setCategories, setError);
  }, [setCategories]);

  const loadMore = (page) => {
    getCategories(
      (more) => {
        if (more) {
          more.content = categories?.content?.concat(more?.content);
          more.number = more.number + categories?.number || 0;
          setCategories(more);
        }
      },
      setError,
      page
    );
  };

  const categoryClick = (event) => {
    event.preventDefault();
    navigate(`/category/${event.target.value}`);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="page home-page">
      <Header />
      <h2>Categories</h2>
      <div>
        <InfiniteScroll
          page={categories}
          renderItem={(category) => (
            <div key={`category-${category.name}`}>
              <button value={category.name} onClick={categoryClick}>
                {category.name}
              </button>
            </div>
          )}
          loadMore={loadMore}
        />
      </div>
    </div>
  );
}

export default withAuth(HomePage);
