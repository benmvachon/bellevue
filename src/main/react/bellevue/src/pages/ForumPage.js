import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext.js';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  deleteForum,
  favoriteForum,
  unfavoriteForum,
  setForumNotification,
  markForumRead,
  onForumUnreadUpdate,
  unsubscribeForumUnread,
  removeFromForum
} from '../api/api.js';
import { updateSearchParams } from '../utils/updateSearchParams.js';
import ExcludedForums from '../components/ExcludedForums.js';
import PostsList from '../components/PostsList.js';
import asPage from '../utils/asPage.js';
import ForumForm from '../components/ForumForm.js';
import Forum from '../components/Forum.js';
import FavoriteButton from '../components/FavoriteButton.js';

function ForumPage() {
  const navigate = useNavigate();
  const { id = '1' } = useParams();
  const { userId } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [forum, setForum] = useState(null);
  const [sortByPopular, setSortByPopular] = useState(
    searchParams.get('popular') === 'true'
  );
  const [onlyTownHallPosts, setOnlyTownHallPosts] = useState(
    searchParams.get('townhall') === 'true'
  );
  const [excludedForums, setExcludedForums] = useState(() => {
    const param = searchParams.get('excluded');
    return param ? param.split(',') : [];
  });
  const [showForumForm, setShowForumForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (id) {
      const forum = id === '1' ? (onlyTownHallPosts ? 1 : undefined) : id;
      onForumUnreadUpdate(forum, () => getForum(id, setForum, setError));
      return () => {
        unsubscribeForumUnread(forum);
      };
    }
  }, [id, onlyTownHallPosts]);

  useEffect(() => {
    if (id) {
      setLoading(true);
      getForum(
        id,
        (forum) => {
          setForum(forum);
          setLoading(false);
        },
        (error) => {
          setError(error);
          setLoading(false);
        }
      );
    }
  }, [id]);

  const favorite = () => {
    favoriteForum(id, () => getForum(id, setForum, setError), setError);
  };

  const unfavorite = () => {
    unfavoriteForum(id, () => getForum(id, setForum, setError), setError);
  };

  const markRead = () => {
    markForumRead(
      forum.id,
      () => getForum(forum.id, setForum, setError),
      setError
    );
  };

  const toggleNotifications = () => {
    setForumNotification(
      forum.id,
      !forum.notify,
      () => getForum(forum.id, setForum, setError),
      setError
    );
  };

  const toggleSort = () => {
    const toggled = !sortByPopular;
    setSortByPopular(toggled);
    updateSearchParams({ popular: toggled }, searchParams, setSearchParams);
  };

  const toggleFilterAll = () => {
    const townhall = !onlyTownHallPosts;
    if (townhall) {
      clearFilter();
    }
    setOnlyTownHallPosts(townhall);
    setExcludedForums([]);
    updateSearchParams(
      { townhall, excluded: [] },
      searchParams,
      setSearchParams
    );
  };

  const excludeForum = (forum) => {
    const excluded = excludedForums.concat([forum]);
    setExcludedForums(excluded);
    updateSearchParams({ excluded }, searchParams, setSearchParams);
  };

  const includeForum = (forum) => {
    const excluded = excludedForums.filter((id) => forum !== id);
    setExcludedForums(excluded);
    updateSearchParams({ excluded }, searchParams, setSearchParams);
  };

  const clearFilter = () => {
    setExcludedForums([]);
    updateSearchParams({ excluded: [] }, searchParams, setSearchParams);
  };

  const removeAccess = () => {
    removeFromForum(forum.id, () => navigate('/'), setError);
  };

  const handleDelete = () => {
    deleteForum(forum.id);
  };

  const closeForumEditor = () => {
    setShowForumForm(false);
    getForum(forum.id, setForum, setError);
  };

  if (error) return JSON.stringify(error);

  if (loading) return <p>Loading...</p>;

  return (
    <div className="page-contents">
      <div className="metadata">
        <div className="building-and-buttons">
          <Forum id={forum.id} forumProp={forum} />
          <div className="forum-actions pixel-corners">
            <p>{forum?.description}</p>
            <div className="forum-buttons">
              <div className="plain-forum-buttons">
                {forum.unreadCount > 0 && (
                  <button onClick={markRead}>mark read</button>
                )}
                <button
                  onClick={toggleNotifications}
                >{`turn ${forum.notify ? 'off' : 'on'} notifications`}</button>
                {id === '1' && (
                  <button onClick={toggleFilterAll}>
                    {onlyTownHallPosts
                      ? 'show all flyers'
                      : 'show only Town Hall flyers'}
                  </button>
                )}
                {forum?.custom && forum?.user?.id !== userId && (
                  <button onClick={removeAccess}>remove access</button>
                )}
                {forum?.user?.id === userId && (
                  <button onClick={() => setShowForumForm(true)}>
                    edit building
                  </button>
                )}
                {forum?.user?.id === userId && (
                  <button onClick={handleDelete}>delete building</button>
                )}
              </div>
              <FavoriteButton
                favorited={forum.favorite}
                onClick={() => {
                  forum.favorite ? unfavorite() : favorite();
                }}
              />
            </div>
          </div>
        </div>
        {id === '1' && !onlyTownHallPosts && (
          <ExcludedForums
            excludedForums={excludedForums}
            includeForum={includeForum}
            clearFilter={clearFilter}
          />
        )}
      </div>
      <div className="contents">
        <PostsList
          key={`posts-list-${forum.id}`}
          id={`posts-list-${forum.id}`}
          forum={forum}
          sortByPopular={sortByPopular}
          toggleSort={toggleSort}
          feed={id === '1' && !onlyTownHallPosts}
          excludedForums={excludedForums}
          excludeForum={id === '1' ? excludeForum : undefined}
        />
      </div>
      <ForumForm
        forum={forum}
        show={showForumForm}
        onClose={closeForumEditor}
      />
    </div>
  );
}

ForumPage.displayName = 'ForumPage';

export default withAuth(asPage(ForumPage, 'forum-page'));
