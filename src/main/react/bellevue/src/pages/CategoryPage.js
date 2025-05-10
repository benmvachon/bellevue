import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getForums, addForum } from '../api/api.js';
import Header from '../components/Header.js';
import Page from '../components/Page.js';

function CategoryPage() {
  const navigate = useNavigate();
  const { category } = useParams();
  const [forums, setForums] = useState(null);
  const [newForumName, setNewForumName] = useState('');
  const [error, setError] = useState(false);

  const refresh = () => {
    setNewForumName('');
    getForums(category, setForums, setError);
  };

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category, setForums]);

  const loadPage = (page) => {
    getForums(setForums, setError, page);
  };

  const forumClick = (event) => {
    event.preventDefault();
    navigate('/forum/' + event.target.value);
  };

  if (error) return JSON.stringify(error);

  return (
    <div className="page category-page">
      <Header />
      <h2>{category}</h2>
      <div>
        <Page
          page={forums}
          renderItem={(forum) => (
            <div key={`forum-${forum.id}`}>
              <button value={forum.id} onClick={forumClick}>
                {forum.name}
              </button>
            </div>
          )}
          loadPage={loadPage}
        />
      </div>
      <form onSubmit={() => addForum(category, newForumName, refresh)}>
        <input
          value={newForumName}
          onChange={(e) => setNewForumName(e.target.value)}
        />
        <button type="submit">Create New</button>
      </form>
    </div>
  );
}

export default withAuth(CategoryPage);
