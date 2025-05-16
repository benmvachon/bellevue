import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  getTotalPosts,
  addPost,
  onForumUpdate,
  favoriteForum,
  unfavoriteForum,
  unsubscribeForum,
  getPost,
  getPopularPosts,
  getRecentPosts,
  onForumPopularityUpdate
} from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';
import Attendees from '../components/Attendees.js';
import ScrollLoader from '../components/ScrollLoader.js';

function ForumPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [forum, setForum] = useState(null);
  const [sortByPopular, setSortByPopular] = useState(false);
  const [posts, setPosts] = useState([]);
  const [totalPosts, setTotalPosts] = useState(0);
  const [newPost, setNewPost] = useState(null);
  const [error, setError] = useState(false);

  const submitPost = (event) => {
    event.preventDefault();
    addPost(id, newPost, () => setNewPost(''), setError);
  };

  const loadMore = () => {
    const cursor1 = sortByPopular
      ? posts[posts.length - 1].popularity
      : posts[posts.length - 1].created.getTime();
    const cursor2 = posts[posts.length - 1].id;
    getTotalPosts(
      id,
      (totalPosts) => {
        let request = getRecentPosts;
        if (sortByPopular) request = getPopularPosts;
        request(
          id,
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
      },
      setError
    );
  };

  const toggleSort = () => {
    setSortByPopular(!sortByPopular);
  };

  const favorite = () => {
    favoriteForum(id, () => getForum(id, setForum, setError), setError);
  };

  const unfavorite = () => {
    unfavoriteForum(id, () => getForum(id, setForum, setError), setError);
  };

  useEffect(() => {
    if (id) {
      getForum(id, setForum, setError);
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      onForumUpdate(id, (postId) => {
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
      if (sortByPopular)
        onForumPopularityUpdate(id, (message) => {
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
                posts.concat([post]).sort((a, b) => a.popularity - b.popularity)
              );
            });
          }
        });
      return () => {
        unsubscribeForum(id);
      };
    }
  }, [id, sortByPopular, posts]);

  useEffect(() => {
    if (id) {
      getTotalPosts(
        id,
        (totalPosts) => {
          let request = getRecentPosts;
          if (sortByPopular) request = getPopularPosts;
          setTotalPosts(totalPosts);
          request(id, setPosts, setError, null, null, 5);
        },
        setError
      );
    }
  }, [id, sortByPopular]);

  if (error) return JSON.stringify(error);
  if (!forum) return;

  return (
    <div className="page forum-page">
      <Header />
      <h2>{forum?.name}</h2>
      <button onClick={() => navigate(`/category/${forum?.category}`)}>
        {forum?.category}
      </button>
      {forum.favorite ? (
        <button onClick={unfavorite}>Unfavorite</button>
      ) : (
        <button onClick={favorite}>Favorite</button>
      )}
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
                depth={0}
                sortByPopularParent={sortByPopular}
              />
            ))}
          </ScrollLoader>
        </div>
      </div>
    </div>
  );
}

export default withAuth(ForumPage);
