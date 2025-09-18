import React, { useEffect, useState } from 'react';
import { useNavigate, useOutletContext, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getPost } from '../api/api.js';
import Post from '../components/Post.js';
import PostObject from '../api/Post.js';

function PostPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { setClassName, setMapSlider, pushAlert } = useOutletContext();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    setClassName('post-page');
    setMapSlider(true);
  }, [setClassName, setMapSlider]);

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
    setLoading(true);
    if (id)
      getPost(
        id,
        (post) => {
          setPost(post);
          setLoading(false);
        },
        () => navigate('/error')
      );
  }, [navigate, id]);

  if (loading) return <p>Loading...</p>;

  const parentFuncs = [];
  let i = 0;
  const getPostElement = (depth) => (
    <Post id={post?.id} postProp={post} depth={depth} selected showForum />
  );

  if (post) {
    let parent = post.parent;
    let previousParent = post;
    while (parent) {
      const id = Number.parseInt('' + parent.id);
      const previousId = Number.parseInt('' + previousParent.id);
      const pointer = Number.parseInt('' + i);
      const post = PostObject.fromJSON(parent.toJSON());
      parentFuncs[pointer] = (depth) => (
        <Post
          id={id}
          postProp={post}
          depth={depth}
          selectedChildId={previousId}
          getSelectedChild={
            pointer < 1 ? getPostElement : parentFuncs[pointer - 1]
          }
          showForum
        />
      );
      i++;
      previousParent = parent;
      parent = parent.parent;
    }
  }

  return (
    <div className="page-contents">
      <div className="contents">
        <div className="posts">
          {i > 0 ? parentFuncs[i - 1](0, true) : getPostElement(0, true)}
        </div>
      </div>
    </div>
  );
}

PostPage.displayName = 'PostPage';

export default withAuth(PostPage);
