import { useCallback, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { useAuth } from '../utils/AuthContext.js';
import {
  addReply,
  getPost,
  ratePost,
  onPostUpdate,
  favoritePost,
  unfavoritePost,
  unsubscribePost,
  getRecentReplies,
  getPopularReplies,
  unsubscribePostPopularity,
  onPostPopularityUpdate,
  markPostRead,
  deletePost,
  onReplyDelete,
  unsubscribeReplyDelete
} from '../api/api.js';
import Rating from './Rating.js';
import ScrollLoader from './ScrollLoader.js';

function Post({
  id,
  postProp,
  showForum = false,
  selected = false,
  selectedChildId,
  getSelectedChild,
  sortByPopularParent = false,
  sortParentList,
  excludeForum,
  depth = 0
}) {
  const navigate = useNavigate();
  const { userId } = useAuth();
  const [post, setPost] = useState(postProp);
  const [sortByPopular, setSortByPopular] = useState(sortByPopularParent);
  const [replies, setReplies] = useState([]);
  const [reply, setReply] = useState(null);
  const [showReplies, setShowReplies] = useState(depth < 2);
  const [error, setError] = useState(false);

  const submitReply = (event) => {
    event.preventDefault();
    addReply(post?.forum?.id, post?.id, reply, () => setReply(''), setError);
  };

  const loadMore = () => {
    const cursor1 = sortByPopular
      ? replies[replies.length - 1].popularity
      : replies[replies.length - 1].created.getTime();
    const cursor2 = replies[replies.length - 1].id;
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
          cursor1,
          cursor2,
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

  const sortReplies = useCallback(
    (id, popularity) => {
      getPost(
        post.id,
        (updatedPost) => {
          if (sortByPopular) {
            let index = -1;
            const reply = replies.find((reply, i) => {
              if (reply.id === id) {
                index = i;
                return true;
              }
              return false;
            });
            reply.popularity = popularity;
            if (index !== replies.length - 1) {
              let updatedReplies = replies.sort((a, b) => {
                if (b.popularity !== a.popularity)
                  return b.popularity - a.popularity;
                return b.id - a.id;
              });
              if (
                updatedReplies.indexOf(reply) === replies.length - 1 &&
                updatedPost.children === replies.length
              ) {
                updatedReplies = updatedReplies.filter(
                  (reply) => reply.id !== id
                );
              }
              setReplies(updatedReplies);
            }
          }
          sortParentList(updatedPost.id, popularity);
          setPost(updatedPost);
        },
        setError
      );
    },
    [replies, post?.id, sortByPopular, sortParentList]
  );

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
      onReplyDelete(id, (message) => {
        getPost(
          id,
          (post) => {
            const updatedReplies = replies.filter(
              (reply) => reply.id !== message
            );
            setPost(post);
            setReplies(updatedReplies);
            sortParentList(id, post.popularity);
          },
          setError
        );
      });
      if (sortByPopular)
        onPostPopularityUpdate(id, (message) => {
          const post = message.post;
          const popularity = message.popularity;
          if (
            replies &&
            replies.length &&
            popularity >= replies[replies.length - 1].popularity &&
            replies.findIndex((reply) => reply.id === post) < 0
          ) {
            getPost(post, (post) => {
              setReplies(
                replies.concat([post]).sort((a, b) => {
                  if (b.popularity !== a.popularity)
                    return b.popularity - a.popularity;
                  return b.id - a.id;
                })
              );
            });
          }
        });
      return () => {
        unsubscribePost(id);
        unsubscribePostPopularity(id);
        unsubscribeReplyDelete(id);
      };
    }
  }, [id, sortByPopular, replies, sortParentList]);

  useEffect(() => {
    if (post?.children > 0 && showReplies) {
      let request = getRecentReplies;
      if (sortByPopular) request = getPopularReplies;
      request(post.id, setReplies, setError, null, null, 5, selectedChildId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [post?.id, showReplies, sortByPopular, selectedChildId]);

  useEffect(() => {
    setSortByPopular(sortByPopularParent);
  }, [sortByPopularParent]);

  useEffect(() => {
    if (!post.read) {
      markPostRead(post.id);
    }
  }, [post.id, post.read]);

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
        {post.user.id === userId && (
          <button onClick={() => deletePost(id)}>Delete</button>
        )}
      </div>
      <p>{post?.content}</p>
      <button onClick={toggleSort}>
        {sortByPopular ? 'Most recent' : 'Most popular'}
      </button>
      {showForum && (
        <button onClick={() => navigate(`/forum/${post?.forum.id}`)}>
          Go to {post.forum.name}
        </button>
      )}
      {excludeForum && (
        <button onClick={() => excludeForum(post.forum.id)}>
          Filter posts from {post.forum.name}
        </button>
      )}
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
                  showForum={showForum}
                  depth={depth + 1}
                  sortByPopularParent={sortByPopular}
                  sortParentList={sortReplies}
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
  showForum: PropTypes.bool,
  selected: PropTypes.bool,
  selectedChildId: PropTypes.number,
  getSelectedChild: PropTypes.func,
  sortByPopularParent: PropTypes.bool,
  sortParentList: PropTypes.func,
  excludeForum: PropTypes.func,
  depth: PropTypes.number
};

export default withAuth(Post);
