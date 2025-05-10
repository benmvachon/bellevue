import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getCategories } from '../api/api.js';
import Header from '../components/Header.js';
import Page from '../components/Page.js';

function HomePage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    getCategories(setCategories, setError);
  }, [setCategories]);

  const loadPage = (page) => {
    getCategories(setCategories, setError, page);
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
        <Page
          page={categories}
          renderItem={(category) => (
            <div key={`category-${category.name}`}>
              <button value={category.name} onClick={categoryClick}>
                {category.name}
              </button>
            </div>
          )}
          loadPage={loadPage}
        />
      </div>
    </div>
  );
}

export default withAuth(HomePage);
