import { useCallback, useEffect, useState } from 'react';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  getTotalPosts,
  addPost,
  favoriteForum,
  unfavoriteForum,
  setForumNotification,
  getPost,
  markForumRead,
  onForumUpdate,
  onForumPopularityUpdate,
  onForumUnreadUpdate,
  onPostDelete,
  unsubscribeForum,
  unsubscribeForumPopularity,
  unsubscribeForumUnread,
  unsubscribePostDelete,
  getPosts,
  getRecentPosts,
  getPopularPosts
} from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';
import Attendees from '../components/Attendees.js';
import ScrollLoader from '../components/ScrollLoader.js';
import ExcludedForums from '../components/ExcludedForums.js';

function FeedPage() {
  const [forum, setForum] = useState(null);
  const [onlyTownHallPosts, setOnlyTownHallPosts] = useState(false);
  const [excludedForums, setExcludedForums] = useState([]);
  const [sortByPopular, setSortByPopular] = useState(false);
  const [posts, setPosts] = useState([]);
  const [totalPosts, setTotalPosts] = useState(0);
  const [newPost, setNewPost] = useState(null);
  const [error, setError] = useState(false);

  const submitPost = (event) => {
    event.preventDefault();
    addPost(1, newPost, () => setNewPost(''), setError);
  };

  const loadMore = () => {
    const cursor1 = sortByPopular
      ? posts[posts.length - 1].popularity
      : posts[posts.length - 1].created.getTime();
    const cursor2 = posts[posts.length - 1].id;
    getTotalPosts(
      onlyTownHallPosts ? 1 : undefined,
      (totalPosts) => {
        if (onlyTownHallPosts) {
          let request = getRecentPosts;
          if (sortByPopular) request = getPopularPosts;
          request(
            1,
            (more) => {
              setTotalPosts(totalPosts);
              if (more) {
                setPosts(posts?.concat(more));
              }
            },
            setError,
            cursor1,
            cursor2,
            5
          );
        } else {
          getPosts(
            (more) => {
              setTotalPosts(totalPosts);
              if (more) {
                setPosts(posts?.concat(more));
              }
            },
            setError,
            cursor1,
            cursor2,
            excludedForums,
            sortByPopular,
            5
          );
        }
      },
      setError,
      excludedForums
    );
  };

  const toggleSort = () => {
    setSortByPopular(!sortByPopular);
  };

  const favorite = () => {
    favoriteForum(1, () => getForum(1, setForum, setError), setError);
  };

  const unfavorite = () => {
    unfavoriteForum(1, () => getForum(1, setForum, setError), setError);
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

  const sortPosts = useCallback(
    (id, popularity) => {
      if (sortByPopular) {
        let index = -1;
        const post = posts.find((post, i) => {
          if (post.id === id) {
            index = i;
            return true;
          }
          return false;
        });
        post.popularity = popularity;
        if (index !== posts.length - 1) {
          let updatedPosts = posts.sort((a, b) => {
            if (b.popularity !== a.popularity)
              return b.popularity - a.popularity;
            return b.id - a.id;
          });
          if (
            updatedPosts.indexOf(post) === posts.length - 1 &&
            totalPosts === posts.length
          ) {
            updatedPosts = updatedPosts.filter((post) => post.id !== id);
          }
          setPosts(updatedPosts);
        }
      }
    },
    [posts, sortByPopular, totalPosts]
  );

  const excludeForum = (forum) => {
    setExcludedForums(excludedForums.concat([forum]));
  };

  const toggleFilterAll = () => {
    if (!onlyTownHallPosts) {
      clearFilter();
    }
    setOnlyTownHallPosts(!onlyTownHallPosts);
  };

  const includeForum = (forum) => {
    setExcludedForums(excludedForums.filter((id) => forum !== id));
  };

  const clearFilter = () => {
    setExcludedForums([]);
  };

  useEffect(() => {
    getForum(1, setForum, setError);
  }, []);

  useEffect(() => {
    onForumUpdate(onlyTownHallPosts ? 1 : undefined, (postId) => {
      if (!sortByPopular) {
        getPost(
          postId,
          (post) => {
            if (post) setPosts([post].concat(posts));
          },
          () => {}
        );
      }
    });
    onPostDelete(onlyTownHallPosts ? 1 : undefined, (message) => {
      getTotalPosts(
        onlyTownHallPosts ? 1 : undefined,
        (totalPosts) => {
          setTotalPosts(totalPosts);
          setPosts(posts.filter((post) => post.id !== message));
        },
        setError,
        excludedForums
      );
    });
    if (sortByPopular)
      onForumPopularityUpdate(onlyTownHallPosts ? 1 : undefined, (message) => {
        const messagePost = message.post;
        const popularity = message.popularity;
        if (
          posts &&
          posts.length &&
          popularity >= posts[posts.length - 1].popularity &&
          posts.findIndex((post) => post.id === messagePost) < 0
        ) {
          getPost(messagePost, (post) => {
            setPosts(
              posts.concat([post]).sort((a, b) => {
                if (b.popularity !== a.popularity)
                  return b.popularity - a.popularity;
                return b.id - a.id;
              })
            );
          });
        }
      });
    onForumUnreadUpdate(onlyTownHallPosts ? 1 : undefined, () =>
      getForum(1, setForum, setError)
    );
    return () => {
      unsubscribeForum(1);
      unsubscribeForumPopularity(1);
      unsubscribePostDelete(1);
      unsubscribeForumUnread(1);
    };
  }, [sortByPopular, posts, onlyTownHallPosts, excludedForums]);

  useEffect(() => {
    getTotalPosts(
      onlyTownHallPosts ? 1 : undefined,
      (totalPosts) => {
        if (onlyTownHallPosts) {
          let request = getRecentPosts;
          if (sortByPopular) request = getPopularPosts;
          setTotalPosts(totalPosts);
          request(1, setPosts, setError, null, null, 5);
        } else {
          setTotalPosts(totalPosts);
          getPosts(
            setPosts,
            setError,
            null,
            null,
            excludedForums,
            sortByPopular,
            5
          );
        }
      },
      setError,
      excludedForums
    );
  }, [onlyTownHallPosts, excludedForums, sortByPopular]);

  if (error) return JSON.stringify(error);
  if (!forum) return;

  return (
    <div className="page forum-page">
      <Header />
      <h2>
        {forum?.name} - ({forum?.unreadCount} unread)
      </h2>
      <h3>{forum?.description}</h3>
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
      <button onClick={toggleFilterAll}>
        {onlyTownHallPosts ? 'Show all posts' : 'Show only Town Hall posts'}
      </button>
      <ExcludedForums
        excludedForums={excludedForums}
        includeForum={includeForum}
        clearFilter={clearFilter}
      />
      <div className="contents">
        <Attendees />
        <div className="posts">
          <h3>Posts</h3>
          <button onClick={toggleSort}>
            {sortByPopular ? 'Most recent' : 'Most popular'}
          </button>
          <form onSubmit={submitPost}>
            <textarea
              value={newPost}
              onChange={(e) => setNewPost(e.target.value)}
            />
            <button type="submit">Post</button>
          </form>
          <ScrollLoader total={totalPosts} loadMore={loadMore}>
            {posts?.map((post) => (
              <Post
                key={`post-${post.id}`}
                id={post.id}
                postProp={post}
                showForum
                depth={0}
                sortByPopularParent={sortByPopular}
                sortParentList={sortPosts}
                excludeForum={excludeForum}
              />
            ))}
          </ScrollLoader>
        </div>
      </div>
    </div>
  );
}

export default withAuth(FeedPage);
