import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  getForum,
  getPosts,
  addPost,
  getFriendsInLocation
} from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';
import InfiniteScroll from '../components/InfiniteScroll.js';

function ForumPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [forum, setForum] = useState(null);
  const [sortByRelevance, setSortByRelevance] = useState(true);
  const [posts, setPosts] = useState(null);
  const [attendees, setAttendees] = useState(null);
  const [newPost, setNewPost] = useState(null);
  const [error, setError] = useState(false);

  const refreshForum = () => getForum(id, setForum, setError);
  const refreshPosts = () =>
    getPosts(id, setPosts, setError, 0, sortByRelevance);
  const refreshAttendees = () =>
    getFriendsInLocation(id, setAttendees, setError);

  const loadMorePosts = (page) => {
    getPosts(
      id,
      (more) => {
        if (more) {
          more.content = posts?.content?.concat(more?.content);
          more.number = more.number + posts?.number || 0;
          setPosts(more);
        }
      },
      setError,
      page,
      sortByRelevance
    );
  };

  const loadMoreAttendees = (page) => {
    getFriendsInLocation(
      id,
      (more) => {
        if (more) {
          more.content = attendees?.content?.concat(more?.content);
          setAttendees(more);
        }
      },
      setError,
      page
    );
  };

  const toggleSort = () => {
    setSortByRelevance(!sortByRelevance);
  };

  useEffect(() => {
    if (id) {
      refreshForum();
      refreshAttendees();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (id) {
      refreshPosts();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, sortByRelevance]);

  if (error) return JSON.stringify(error);
  if (!forum) return;

  return (
    <div className="page forum-page">
      <Header />
      <h2>{forum?.name}</h2>
      <button onClick={() => navigate(`/category/${forum?.category}`)}>
        {forum?.category}
      </button>
      <div className="contents">
        <div className="attendees">
          <h3>Attendees</h3>
          <InfiniteScroll
            page={attendees}
            renderItem={(attendee) => (
              <button onClick={() => navigate(`/profile/${attendee.id}`)}>
                {attendee.name}
              </button>
            )}
            loadMore={loadMoreAttendees}
          />
        </div>
        <div className="posts">
          <h3>Posts</h3>
          <button onClick={toggleSort}>
            {sortByRelevance ? 'Most recent' : 'Most relevant'}
          </button>
          <form
            onSubmit={() => {
              addPost(id, newPost, refreshPosts);
            }}
          >
            <textarea
              value={newPost}
              onChange={(e) => setNewPost(e.target.value)}
            />
            <button type="submit">Post</button>
          </form>
          <InfiniteScroll
            page={posts}
            renderItem={(post) => (
              <Post
                key={`post-${post.id}`}
                id={post.id}
                depth={0}
                parentSortedByRelevance={sortByRelevance}
              />
            )}
            loadMore={loadMorePosts}
          />
        </div>
      </div>
    </div>
  );
}

export default withAuth(ForumPage);
