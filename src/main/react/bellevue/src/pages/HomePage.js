import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getCategories } from '../api/api.js';
import Header from '../components/Header.js';

function HomePage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState(null);

  useEffect(() => {
    getCategories(setCategories, setCategories);
  }, [setCategories]);

  const categoryClick = (event) => {
    event.preventDefault();
    navigate(`/category/${event.target.value}`);
  };

  return (
    <div className="page home-page">
      <Header />
      <h2>Categories</h2>
      <div>
        {categories?.content?.map((category) => (
          <div key={`category-${category.name}`}>
            <button value={category.name} onClick={categoryClick}>
              {category.name}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default withAuth(HomePage);
