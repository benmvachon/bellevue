import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { addReply, getPost, getReplies, ratePost } from '../api/api.js';
import Rating from './Rating.js';
import InfiniteScroll from './InfiniteScroll.js';

function Post({ id, children }) {
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [replies, setReplies] = useState(null);
  const [reply, setReply] = useState(null);
  const [showReplies, setShowReplies] = useState(true);
  const [error, setError] = useState(false);

  const refresh = () => {
    getPost(id, setPost, setError);
  };

  const refreshChildren = () => {
    getReplies(id, setReplies, setError);
  };

  const loadMore = (page) => {
    getReplies(
      id,
      (more) => {
        if (more) {
          more.content = replies?.content?.concat(more?.content);
          more.number = more.number + replies?.number || 0;
          setReplies(more);
        }
      },
      setError,
      page
    );
  };

  useEffect(() => {
    if (id) refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (post && post.children > 0) refreshChildren();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [post]);

  if (error) return JSON.stringify(error);
  if (!post) return;
  if (!children) {
    children = [];
    if (post.children > 0) {
      children.push(
        <button onClick={() => setShowReplies(!showReplies)}>
          {showReplies
            ? `Hide (${post.children}) replies`
            : `Show (${post.children}) replies`}
        </button>
      );
      if (showReplies) {
        children.push(
          <InfiniteScroll
            page={replies}
            renderItem={(reply) => (
              <Post key={`post-${reply.id}`} id={reply.id} />
            )}
            loadMore={loadMore}
          />
        );
      }
    }
  }

  return (
    <div className="post">
      <div className="post-header">
        <button onClick={() => navigate(`/profile/${post?.user.id}`)}>
          {post?.user.name}
        </button>
        <p>{post?.created?.toLocaleString()}</p>
        <Rating
          rating={post?.rating}
          ratingCount={post?.ratingCount}
          onClick={(rating) => {
            ratePost(post?.id, rating, refresh, refresh);
          }}
        />
      </div>
      <p>{post?.content}</p>
      <form
        onSubmit={() => {
          addReply(post?.forum?.id, post?.id, reply, refreshChildren);
        }}
      >
        <textarea value={reply} onChange={(e) => setReply(e.target.value)} />
        <button type="submit">Reply</button>
      </form>
      {children}
    </div>
  );
}

Post.propTypes = {
  id: PropTypes.number,
  children: PropTypes.element
};

export default withAuth(Post);
