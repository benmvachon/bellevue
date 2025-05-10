import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  addReply,
  getPost,
  getReplies,
  ratePost,
  onPostUpdate,
  favoritePost,
  unfavoritePost,
  unsubscribePost
} from '../api/api.js';
import Rating from './Rating.js';
import ScrollLoader from './ScrollLoader.js';

function Post({
  id,
  selected = false,
  selectedChildId,
  getSelectedChild,
  depth = 0
}) {
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [replies, setReplies] = useState([]);
  const [reply, setReply] = useState(null);
  const [showReplies, setShowReplies] = useState(depth < 2);
  const [error, setError] = useState(false);

  const submitReply = (event) => {
    event.preventDefault();
    addReply(
      post?.forum?.id,
      post?.id,
      reply,
      (reply) => {
        setReply('');
        getPost(
          id,
          (post) => {
            setPost(post);
            setReplies([reply].concat(replies));
          },
          setError
        );
      },
      setError
    );
  };

  const loadMore = () => {
    const cursor = replies[replies.length - 1].created.getTime();
    getReplies(
      id,
      (more) => {
        if (more) {
          setReplies(replies.concat(more));
        }
      },
      setError,
      cursor,
      1,
      selectedChildId
    );
  };

  const favorite = () => {
    favoritePost(id, () => getPost(id, setPost, setError), setError);
  };

  const unfavorite = () => {
    unfavoritePost(id, () => getPost(id, setPost, setError), setError);
  };

  useEffect(() => {
    if (id) {
      getPost(id, setPost, setError);
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      onPostUpdate(id, (event) => {
        getPost(
          id,
          (post) => {
            setPost(post);
            if (!event.rating) setReplies([event.post].concat(replies));
          },
          setError
        );
      });
      return () => {
        unsubscribePost(id);
      };
    }
  }, [id, replies]);

  useEffect(() => {
    if (post && post.children > 0 && showReplies)
      getReplies(post.id, setReplies, setError, null, 1, selectedChildId);
  }, [post, showReplies, selectedChildId]);

  if (error) return JSON.stringify(error);
  if (!post) return;

  return (
    <div className={selected ? 'selected post' : 'post'}>
      <div className="post-header">
        <button onClick={() => navigate(`/profile/${post?.user.id}`)}>
          {post?.user.name}
        </button>
        <p>{post?.created?.toLocaleString()}</p>
        <Rating
          rating={post?.rating}
          ratingCount={post?.ratingCount}
          onClick={(rating) => {
            ratePost(
              post?.id,
              rating,
              () => getPost(id, setPost, setError),
              setError
            );
          }}
        />
        {selected ? undefined : (
          <button onClick={() => navigate(`/post/${post?.id}`)}>
            Go to post
          </button>
        )}
        {post.favorite ? (
          <button onClick={unfavorite}>Unfavorite</button>
        ) : (
          <button onClick={favorite}>Favorite</button>
        )}
      </div>
      <p>{post?.content}</p>
      <form onSubmit={submitReply}>
        <textarea value={reply} onChange={(e) => setReply(e.target.value)} />
        <button type="submit">Reply</button>
      </form>
      {post.children > 0 && (
        <div className="post-children-container">
          <button onClick={() => setShowReplies(!showReplies)}>
            {showReplies
              ? `Hide (${post.children}) replies`
              : `Show (${post.children}) replies`}
          </button>
          {showReplies && getSelectedChild && getSelectedChild(depth)}
          {showReplies && (
            <ScrollLoader total={post.children} loadMore={loadMore}>
              {replies?.map((post) => (
                <Post key={`post-${post.id}`} id={post.id} depth={depth + 1} />
              ))}
            </ScrollLoader>
          )}
        </div>
      )}
    </div>
  );
}

Post.propTypes = {
  id: PropTypes.number,
  selected: PropTypes.bool,
  selectedChildId: PropTypes.number,
  getSelectedChild: PropTypes.func,
  depth: PropTypes.number
};

export default withAuth(Post);
