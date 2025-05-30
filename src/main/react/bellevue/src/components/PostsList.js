import React, { useCallback, useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import {
  getTotalPosts,
  getPost,
  getPopularPosts,
  getRecentPosts,
  onForumUpdate,
  onForumPopularityUpdate,
  onPostDelete,
  unsubscribeForum,
  unsubscribeForumPopularity,
  unsubscribePostDelete,
  getPosts
} from '../api/api.js';
import Post from '../components/Post.js';
import ScrollLoader from '../components/ScrollLoader.js';
import PostForm from '../components/PostForm.js';

function PostsList({
  forum,
  toggleSort,
  sortByPopular = false,
  feed = false,
  excludedForums = [],
  excludeForum
}) {
  const [posts, setPosts] = useState([]);
  const [totalPosts, setTotalPosts] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const pageSize = 3;

  const loadMore = () => {
    const cursor1 = sortByPopular
      ? posts[posts.length - 1].popularity
      : posts[posts.length - 1].created.getTime();
    const cursor2 = posts[posts.length - 1].id;
    getTotalPosts(
      feed ? undefined : forum.id,
      (totalPosts) => {
        if (feed) {
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
            pageSize
          );
        } else {
          let request = getRecentPosts;
          if (sortByPopular) request = getPopularPosts;
          request(
            feed ? undefined : forum.id,
            (more) => {
              setTotalPosts(totalPosts);
              if (more) {
                setPosts(posts?.concat(more));
              }
            },
            setError,
            cursor1,
            cursor2,
            pageSize
          );
        }
      },
      setError,
      excludedForums
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

  useEffect(() => {
    if (forum.id) {
      onForumUpdate(feed ? undefined : forum.id, (postId) => {
        if (!sortByPopular) {
          getPost(
            postId,
            (post) => {
              if (post) {
                if (
                  !feed ||
                  excludedForums.findIndex((forum) => forum === post.forum.id) <
                    0
                )
                  setPosts([post].concat(posts));
              }
            },
            () => {}
          );
        }
      });
      onPostDelete(feed ? undefined : forum.id, (message) => {
        getTotalPosts(
          feed ? undefined : forum.id,
          (totalPosts) => {
            setTotalPosts(totalPosts);
            setPosts(posts.filter((post) => post.id !== message));
          },
          setError,
          excludedForums
        );
      });
      if (sortByPopular)
        onForumPopularityUpdate(feed ? undefined : forum.id, (message) => {
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
      return () => {
        unsubscribeForum(feed ? undefined : forum.id);
        unsubscribeForumPopularity(feed ? undefined : forum.id);
        unsubscribePostDelete(feed ? undefined : forum.id);
      };
    }
  }, [forum.id, sortByPopular, posts, feed, excludedForums]);

  useEffect(() => {
    if (forum.id) {
      setLoading(true);
      getTotalPosts(
        feed ? undefined : forum.id,
        (totalPosts) => {
          if (feed) {
            getPosts(
              (posts) => {
                setPosts(posts);
                setTotalPosts(totalPosts);
                setLoading(false);
              },
              (error) => {
                setError(error);
                setLoading(false);
              },
              null,
              null,
              excludedForums,
              sortByPopular,
              pageSize
            );
          } else {
            let request = getRecentPosts;
            if (sortByPopular) request = getPopularPosts;
            setTotalPosts(totalPosts);
            request(
              forum.id,
              (posts) => {
                setPosts(posts);
                setLoading(false);
              },
              (error) => {
                setError(error);
                setLoading(false);
              },
              null,
              null,
              pageSize
            );
          }
        },
        (error) => {
          setError(error);
          setLoading(false);
        },
        excludedForums
      );
    }
  }, [forum.id, sortByPopular, feed, excludedForums]);

  if (error) return JSON.stringify(error);

  return (
    <div className="posts">
      <div className="posts-header">
        <h3>Posts</h3>
        <button onClick={toggleSort}>
          {sortByPopular ? 'Most recent' : 'Most popular'}
        </button>
        <PostForm forum={forum} enableForumSelection={feed} />
      </div>
      {loading ? (
        <p>Loading...</p>
      ) : (
        <ScrollLoader total={totalPosts} loadMore={loadMore}>
          {posts?.map((post) => (
            <Post
              key={`post-${post.id}`}
              id={post.id}
              postProp={post}
              showForum={feed}
              depth={0}
              sortByPopularParent={sortByPopular}
              sortParentList={sortPosts}
              excludeForum={excludeForum}
            />
          ))}
        </ScrollLoader>
      )}
    </div>
  );
}

PostsList.propTypes = {
  forum: PropTypes.object.isRequired,
  toggleSort: PropTypes.func.isRequired,
  excludedForums: PropTypes.array,
  excludeForum: PropTypes.func,
  sortByPopular: PropTypes.bool,
  feed: PropTypes.bool
};

PostsList.displayName = 'PostsList';

export default withAuth(PostsList);
