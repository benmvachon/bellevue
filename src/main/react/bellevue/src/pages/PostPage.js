import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getPost } from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';

function PostPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [post, setPost] = useState(null);
  const [error, setError] = useState(false);

  const refreshPost = () => getPost(id, setPost, setError);

  useEffect(() => {
    if (id) {
      refreshPost();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, setPost]);

  if (error) return JSON.stringify(error);
  if (!post) return;

  const getPostElement = (depth, sortByRelevance) => (
    <Post
      id={post.id}
      depth={depth}
      parentSortedByRelevance={sortByRelevance}
      selected
    />
  );
  let parent = post.parent;
  let previousParent = post;
  const parentFuncs = [];
  let i = 0;
  while (parent) {
    const id = Number.parseInt('' + parent.id);
    const previousId = Number.parseInt('' + previousParent.id);
    const pointer = Number.parseInt('' + i);
    parentFuncs[pointer] = (depth, sortByRelevance) => (
      <Post
        id={id}
        depth={depth}
        parentSortedByRelevance={sortByRelevance}
        selectedChildId={previousId}
        getSelectedChild={
          pointer < 1 ? getPostElement : parentFuncs[pointer - 1]
        }
      />
    );
    i++;
    previousParent = parent;
    parent = parent.parent;
  }

  return (
    <div className="page post-page">
      <Header />
      <h2>{post.forum.name}</h2>
      <button onClick={() => navigate(`/forum/${post.forum.id}`)}>
        Back to forum
      </button>
      <div className="contents">
        <div className="posts">
          {i > 0 ? parentFuncs[i - 1](0, true) : getPostElement(0, true)}
        </div>
      </div>
    </div>
  );
}

export default withAuth(PostPage);
