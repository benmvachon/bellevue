import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  getPosts,
  getTotalPosts,
  addPost,
  onForumUpdate,
  favoriteForum,
  unfavoriteForum,
  unsubscribeForum
} from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';
import Attendees from '../components/Attendees.js';
import ScrollLoader from '../components/ScrollLoader.js';

function ForumPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [forum, setForum] = useState(null);
  const [posts, setPosts] = useState([]);
  const [totalPosts, setTotalPosts] = useState(0);
  const [newPost, setNewPost] = useState(null);
  const [error, setError] = useState(false);

  const submitPost = (event) => {
    event.preventDefault();
    addPost(
      id,
      newPost,
      (newPost) => {
        setNewPost('');
        setPosts([newPost].concat(posts));
      },
      setError
    );
  };

  const loadMore = () => {
    const cursor = posts[posts.length - 1].created.getTime();
    getTotalPosts(
      id,
      (totalPosts) => {
        getPosts(
          id,
          (more) => {
            setTotalPosts(totalPosts);
            if (more) {
              setPosts(posts?.concat(more));
            }
          },
          setError,
          cursor,
          5
        );
      },
      setError
    );
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
      onForumUpdate(id, (event) => {
        setPosts([event.post].concat(posts));
      });
      return () => {
        unsubscribeForum(id);
      };
    }
  }, [id, posts]);

  useEffect(() => {
    if (id) {
      getTotalPosts(
        id,
        (totalPosts) => {
          setTotalPosts(totalPosts);
          getPosts(id, setPosts, setError, null, 5);
        },
        setError
      );
    }
  }, [id]);

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
          <form onSubmit={submitPost}>
            <textarea
              value={newPost}
              onChange={(e) => setNewPost(e.target.value)}
            />
            <button type="submit">Post</button>
          </form>
          <ScrollLoader total={totalPosts} loadMore={loadMore}>
            {posts?.map((post) => (
              <Post key={`post-${post.id}`} id={post.id} depth={0} />
            ))}
          </ScrollLoader>
        </div>
      </div>
    </div>
  );
}

export default withAuth(ForumPage);
