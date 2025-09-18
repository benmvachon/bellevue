import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from '../utils/AuthContext.js';
import {
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
import PostForm from './PostForm.js';
import Avatar from './Avatar.js';
import { formatTimeAgo } from '../utils/DateFormatter.js';
import { formatContent } from '../utils/ContentFormatter.js';
import FavoriteButton from './FavoriteButton.js';
import ConfirmationDialog from './ConfirmationDialog.js';

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
  const { pushAlert } = useOutletContext();
  const [post, setPost] = useState(postProp);
  const [sortByPopular, setSortByPopular] = useState(sortByPopularParent);
  const [replies, setReplies] = useState([]);
  const [showReplies, setShowReplies] = useState(depth < 2);
  const [showConfirmationDialog, setShowConfirmationDialog] = useState(false);
  const [error, setError] = useState(false);

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
    if (!post.read) {
      setTimeout(() => {
        markPostRead(post.id);
      }, 100);
    }
  }, [post.id, post.read]);

  useEffect(() => {
    if (id && id !== postProp?.id) {
      getPost(id, setPost, setError);
    } else if (postProp) {
      setPost(postProp);
    }
  }, [id, postProp]);

  useEffect(() => {
    setSortByPopular(sortByPopularParent);
  }, [sortByPopularParent]);

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

  if (!post) return;

  let size = 'sm';
  if (post?.content?.length <= 250) size = 'md';
  if (post?.content?.length <= 50) size = 'lg';
  if (post?.content?.length <= 10) size = 'xl';

  return (
    <div className={selected ? 'selected post' : 'post'}>
      <div className="post-actions">
        <p>{formatTimeAgo(post.created)}</p>
        {post.user.id === userId && (
          <button
            className="delete"
            onClick={() => setShowConfirmationDialog(true)}
          >
            delete
          </button>
        )}
        {showForum && (
          <button onClick={() => navigate(`/town/${post?.forum.id}`)}>
            go to {post.forum.name}
          </button>
        )}
        {excludeForum && post.forum.id !== 1 && (
          <button
            className="filter"
            onClick={() => excludeForum(post.forum.id)}
          >
            filter flyers from {post.forum.name}
          </button>
        )}
        <Rating
          rating={post?.rating}
          ratingCount={post?.ratingCount}
          onClick={(rating) => {
            ratePost(post?.id, rating, undefined, setError);
          }}
        />
      </div>
      <div className="post-contents">
        <Avatar userProp={post?.user} userId={post?.user?.id} />
        <button
          onClick={() => !selected && navigate(`/flyer/${post?.id}`)}
          className={`post-button pixel-corners ${size}`}
          dangerouslySetInnerHTML={{ __html: formatContent(post?.content) }}
        />
        <FavoriteButton
          favorited={post.favorite}
          onClick={() => {
            post.favorite ? unfavorite() : favorite();
          }}
        />
      </div>
      <PostForm forum={post.forum} parent={post} />
      {post.children > 0 && (
        <div className="post-children-container">
          <button onClick={() => setShowReplies(!showReplies)}>
            {showReplies
              ? `hide (${post.children}) replies`
              : `show (${post.children}) replies`}
          </button>
          {showReplies && (
            <button className="sort-button" onClick={toggleSort}>
              &darr; {sortByPopular ? 'most recent' : 'most popular'} &darr;
            </button>
          )}
          {showReplies && getSelectedChild && getSelectedChild(depth)}
          {showReplies && (
            <ScrollLoader
              total={selectedChildId ? post.children - 1 : post.children}
              loadMore={loadMore}
              className="post-children"
            >
              {replies?.map((post) => (
                <Post
                  key={`post-${post.id}`}
                  id={post.id}
                  postProp={post}
                  showForum={showForum}
                  depth={depth + 1}
                  sortByPopularParent={sortByPopular}
                  sortParentList={sortReplies}
                  excludeForum={excludeForum}
                />
              ))}
            </ScrollLoader>
          )}
        </div>
      )}
      <ConfirmationDialog
        show={showConfirmationDialog}
        onConfirm={() => {
          deletePost(id);
          setShowConfirmationDialog(false);
        }}
        onCancel={() => setShowConfirmationDialog(false)}
      />
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

Post.displayName = 'Post';

export default Post;
