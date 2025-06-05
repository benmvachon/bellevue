import { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
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
  unsubscribeForumUnread
} from '../api/api.js';
import { updateSearchParams } from '../utils/updateSearchParams.js';
import Attendees from '../components/Attendees.js';
import ExcludedForums from '../components/ExcludedForums.js';
import PostsList from '../components/PostsList.js';
import asPage from '../utils/asPage.js';
import ForumForm from '../components/ForumForm.js';

function ForumPage() {
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
    updateSearchParams({ townhall }, searchParams, setSearchParams);
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

  const handleDelete = () => {
    deleteForum(forum.id);
  };

  const closeForumEditor = () => {
    setShowForumForm(false);
    getForum(forum.id, setForum, setError);
  };

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

  if (error) return JSON.stringify(error);

  if (loading) return <p>Loading...</p>;

  return (
    <div className="page-contents">
      <div className="metadata">
        <h2>
          {forum?.name} - ({forum?.unreadCount} unread)
        </h2>
        <h3>{forum?.description}</h3>
        <div className="forum-actions">
          {forum.favorite ? (
            <button onClick={unfavorite}>Unfavorite</button>
          ) : (
            <button onClick={favorite}>Favorite</button>
          )}
          {forum.unreadCount > 0 && (
            <button onClick={markRead}>Mark as read</button>
          )}
          <button
            onClick={toggleNotifications}
          >{`Turn ${forum.notify ? 'off' : 'on'} notifications`}</button>
          {id === '1' && (
            <button onClick={toggleFilterAll}>
              {onlyTownHallPosts
                ? 'Show all posts'
                : 'Show only Town Hall posts'}
            </button>
          )}
          {forum?.user?.id === userId && (
            <button onClick={() => setShowForumForm(true)}>Edit Forum</button>
          )}
          {forum?.user?.id === userId && (
            <button onClick={handleDelete}>Delete Forum</button>
          )}
        </div>
        <div className="metadata-lists">
          {id === '1' && (
            <ExcludedForums
              excludedForums={excludedForums}
              includeForum={includeForum}
              clearFilter={clearFilter}
            />
          )}
          <Attendees />
        </div>
      </div>
      <div className="contents">
        <PostsList
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
