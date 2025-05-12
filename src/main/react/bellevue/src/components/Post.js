import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import {
  addReply,
  getPost,
  ratePost,
  onPostUpdate,
  favoritePost,
  unfavoritePost,
  unsubscribePost,
  getRecentReplies,
  getPopularReplies
} from '../api/api.js';
import Rating from './Rating.js';
import ScrollLoader from './ScrollLoader.js';

function Post({
  id,
  postProp,
  selected = false,
  selectedChildId,
  getSelectedChild,
  depth = 0
}) {
  const navigate = useNavigate();
  const [post, setPost] = useState(postProp);
  const [sortByPopular, setSortByPopular] = useState(false);
  const [replies, setReplies] = useState([]);
  const [reply, setReply] = useState(null);
  const [showReplies, setShowReplies] = useState(depth < 2);
  const [error, setError] = useState(false);

  const submitReply = (event) => {
    event.preventDefault();
    addReply(post?.forum?.id, post?.id, reply, () => setReply(''), setError);
  };

  const loadMore = () => {
    const cursor = replies[replies.length - 1].created.getTime();
    getPost(
      id,
      (post) => {
        let request = getRecentReplies;
        if (sortByPopular) request = getPopularReplies;
        request(
          id,
          (more) => {
            setPost(post);
            if (more) {
              setReplies(replies?.concat(more));
            }
          },
          setError,
          sortByPopular ? replies.length : cursor,
          5,
          selectedChildId
        );
      },
      setError
    );
  };

  const toggleSort = () => {
    setSortByPopular(!sortByPopular);
  };

  const favorite = () => {
    favoritePost(id, () => getPost(id, setPost, setError), setError);
  };

  const unfavorite = () => {
    unfavoritePost(id, () => getPost(id, setPost, setError), setError);
  };

  useEffect(() => {
    if (id && id !== postProp?.id) {
      getPost(id, setPost, setError);
    } else if (postProp) {
      setPost(postProp);
    }
  }, [id, postProp]);

  useEffect(() => {
    if (id) {
      onPostUpdate(id, (message) => {
        getPost(
          id,
          (post) => {
            if (sortByPopular || message === 'rating') setPost(post);
            else {
              getPost(
                message,
                (reply) => {
                  setPost(post);
                  if (reply) setReplies([reply].concat(replies));
                },
                () => {}
              );
            }
          },
          setError
        );
      });
      return () => {
        unsubscribePost(id);
      };
    }
  }, [id, sortByPopular, replies]);

  useEffect(() => {
    if (post?.children > 0 && showReplies) {
      if (sortByPopular)
        getPopularReplies(
          post.id,
          setReplies,
          setError,
          null,
          5,
          selectedChildId
        );
      else
        getRecentReplies(
          post.id,
          setReplies,
          setError,
          null,
          5,
          selectedChildId
        );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [post?.id, showReplies, sortByPopular, selectedChildId]);

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
            ratePost(post?.id, rating, undefined, setError);
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
      <button onClick={toggleSort}>
        {sortByPopular ? 'Most recent' : 'Most popular'}
      </button>
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
                <Post
                  key={`post-${post.id}`}
                  id={post.id}
                  postProp={post}
                  depth={depth + 1}
                />
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
  postProp: PropTypes.object,
  selected: PropTypes.bool,
  selectedChildId: PropTypes.number,
  getSelectedChild: PropTypes.func,
  depth: PropTypes.number
};

export default withAuth(Post);
