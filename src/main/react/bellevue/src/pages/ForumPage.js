import { useEffect, useState } from 'react';
import {
  useNavigate,
  useOutletContext,
  useParams,
  useSearchParams
} from 'react-router-dom';
import { useAuth } from '../utils/AuthContext.js';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  deleteForum,
  favoriteForum,
  unfavoriteForum,
  setForumNotification,
  markForumRead,
  markPostsRead,
  onForumUnreadUpdate,
  unsubscribeForumUnread,
  removeFromForum
} from '../api/api.js';
import { updateSearchParams } from '../utils/updateSearchParams.js';
import ExcludedForums from '../components/ExcludedForums.js';
import PostsList from '../components/PostsList.js';
import ForumForm from '../components/ForumForm.js';
import Forum from '../components/Forum.js';
import FavoriteButton from '../components/FavoriteButton.js';
import ConfirmationDialog from '../components/ConfirmationDialog.js';
import ImageButton from '../components/ImageButton.js';
import LoadingSpinner from '../components/LoadingSpinner.js';

function ForumPage() {
  const navigate = useNavigate();
  const { id = '1' } = useParams();
  const { userId } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const { setClassName, setMapSlider, pushAlert } = useOutletContext();
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
  const [showConfirmationDialog, setShowConfirmationDialog] = useState(false);
  const [onConfirm, setOnConfirm] = useState(() => {});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    setClassName('forum-page');
    setMapSlider(true);
  }, [setClassName, setMapSlider]);

  useEffect(() => {
    if (error) {
      pushAlert({
        key: JSON.stringify(error),
        type: 'error',
        content: (
          <div>
            <h3>Error: {error.code}</h3>
            <p>{error.message}</p>
          </div>
        )
      });
      setError(false);
    }
  }, [pushAlert, error]);

  useEffect(() => {
    if (id) {
      setError(false);
      const forum = id === '1' ? (onlyTownHallPosts ? 1 : undefined) : id;
      onForumUnreadUpdate(forum, () => getForum(id, setForum, setError));
      return () => {
        unsubscribeForumUnread(forum);
      };
    }
  }, [id, onlyTownHallPosts]);

  useEffect(() => {
    if (id) {
      setError(false);
      setLoading(true);
      getForum(
        id,
        (forum) => {
          setForum(forum);
          setLoading(false);
        },
        () => navigate('/error')
      );
    }
  }, [navigate, id]);

  const favorite = () => {
    setError(false);
    favoriteForum(id, () => getForum(id, setForum, setError), setError);
  };

  const unfavorite = () => {
    setError(false);
    unfavoriteForum(id, () => getForum(id, setForum, setError), setError);
  };

  const markRead = () => {
    setError(false);
    if ('' + id === '1' && !onlyTownHallPosts)
      markPostsRead(() => getForum(id, setForum, setError), setError);
    else markForumRead(id, () => getForum(id, setForum, setError), setError);
  };

  const toggleNotifications = () => {
    setError(false);
    setForumNotification(
      id,
      !forum.notify,
      () => getForum(id, setForum, setError),
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
    setError(false);
    removeFromForum(id, () => navigate('/'), setError);
    navigate('/');
  };

  const handleDelete = () => {
    setError(false);
    deleteForum(id, () => navigate('/'), setError);
    navigate('/');
  };

  const closeForumEditor = () => {
    setError(false);
    setShowForumForm(false);
    getForum(id, setForum, setError);
  };

  return (
    <div className="page-contents">
      <div className="metadata">
        {loading ? (
          <LoadingSpinner onClick={() => {}} />
        ) : (
          <div className="building-and-buttons">
            <Forum id={id} forumProp={forum} />
            <div className="forum-actions pixel-corners">
              <p>{forum?.description}</p>
              <div className="forum-buttons">
                <div className="plain-forum-buttons">
                  {forum.unreadCount > 0 && (
                    <button onClick={markRead}>mark read</button>
                  )}
                </div>
                <div className="image-forum-buttons">
                  <ImageButton name="newspaper" onClick={toggleNotifications}>
                    <span>{forum.notify ? 'dont notify' : 'notify'}</span>
                  </ImageButton>
                  {id === '1' && (
                    <ImageButton name="townhall" onClick={toggleFilterAll}>
                      <span>
                        {onlyTownHallPosts ? 'show all' : 'Town Hall only'}
                      </span>
                    </ImageButton>
                  )}
                  {forum?.custom && forum?.user?.id !== userId && (
                    <ImageButton
                      name="move-out"
                      onClick={() => {
                        setOnConfirm(() => {
                          return removeAccess;
                        });
                        setShowConfirmationDialog(true);
                      }}
                    >
                      <span>leave</span>
                    </ImageButton>
                  )}
                  {forum?.user?.id === userId && (
                    <ImageButton
                      name="custom-building"
                      onClick={() => setShowForumForm(true)}
                    >
                      <span>edit</span>
                    </ImageButton>
                  )}
                  {forum?.user?.id === userId && (
                    <ImageButton
                      name="bulldozer"
                      onClick={() => {
                        setOnConfirm(() => {
                          return handleDelete;
                        });
                        setShowConfirmationDialog(true);
                      }}
                    >
                      <span>delete</span>
                    </ImageButton>
                  )}
                  <FavoriteButton
                    favorited={forum.favorite}
                    onClick={() => {
                      forum.favorite ? unfavorite() : favorite();
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        )}
        {id === '1' && !onlyTownHallPosts && (
          <ExcludedForums
            excludedForums={excludedForums}
            includeForum={includeForum}
            clearFilter={clearFilter}
          />
        )}
      </div>
      <div className="contents">
        {loading ? (
          <LoadingSpinner onClick={() => {}} />
        ) : (
          <PostsList
            key={`posts-list-${id}`}
            id={`posts-list-${id}`}
            forum={forum}
            sortByPopular={sortByPopular}
            toggleSort={toggleSort}
            feed={id === '1' && !onlyTownHallPosts}
            excludedForums={excludedForums}
            excludeForum={id === '1' ? excludeForum : undefined}
          />
        )}
      </div>
      <ForumForm
        forum={forum}
        show={showForumForm}
        onClose={closeForumEditor}
      />
      <ConfirmationDialog
        show={showConfirmationDialog}
        onConfirm={() => {
          onConfirm();
          setShowConfirmationDialog(false);
        }}
        onCancel={() => setShowConfirmationDialog(false)}
      />
    </div>
  );
}

ForumPage.displayName = 'ForumPage';

export default withAuth(ForumPage);
