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
import InfiniteScroll from './InfiniteScroll.js';

function Post({
  id,
  selected = false,
  selectedChildId,
  getSelectedChild,
  parentSortedByRelevance = true,
  depth = 0
}) {
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [sortByRelevance, setSortByRelevance] = useState(
    parentSortedByRelevance
  );
  const [replies, setReplies] = useState(null);
  const [reply, setReply] = useState(null);
  const [showReplies, setShowReplies] = useState(depth < 2);
  const [error, setError] = useState(false);

  const pageSize = depth < 1 ? 10 : depth < 2 ? 5 : 2;

  const refresh = () => {
    getPost(id, setPost, setError);
  };

  const refreshChildren = () => {
    showReplies &&
      getReplies(
        id,
        setReplies,
        setError,
        0,
        sortByRelevance,
        pageSize,
        selectedChildId
      );
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
      page,
      sortByRelevance,
      pageSize,
      selectedChildId
    );
  };

  const toggleSort = () => {
    setSortByRelevance(!sortByRelevance);
  };

  const favorite = () => {
    favoritePost(id, refresh, setError);
  };

  const unfavorite = () => {
    unfavoritePost(id, refresh, setError);
  };

  useEffect(() => {
    if (id) refresh();
    onPostUpdate(id, refresh);
    return () => {
      unsubscribePost(id);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    setSortByRelevance(parentSortedByRelevance);
  }, [parentSortedByRelevance]);

  useEffect(() => {
    if (post && post.children > 0) refreshChildren();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [post, sortByRelevance, showReplies, selectedChildId]);

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
            ratePost(post?.id, rating, refresh, refresh);
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
      <form
        onSubmit={() => {
          addReply(post?.forum?.id, post?.id, reply, refreshChildren);
        }}
      >
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
          <button onClick={toggleSort}>
            {sortByRelevance ? 'Most recent' : 'Most relevant'}
          </button>
          {showReplies &&
            getSelectedChild &&
            getSelectedChild(depth, sortByRelevance)}
          {showReplies && (
            <InfiniteScroll
              page={replies}
              renderItem={(reply) => (
                <Post
                  key={`post-${reply.id}`}
                  id={reply.id}
                  depth={depth + 1}
                  parentSortedByRelevance={sortByRelevance}
                />
              )}
              loadMore={loadMore}
            />
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
  parentSortedByRelevance: PropTypes.bool,
  depth: PropTypes.number
};

export default withAuth(Post);
