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

  let postElement = (
    <div className="selected-post">
      <Post id={post.id} />
    </div>
  );
  let parent = post.parent;
  while (parent) {
    postElement = <Post id={parent.id}>{postElement}</Post>;
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
        <div className="posts">{postElement}</div>
      </div>
    </div>
  );
}

export default withAuth(PostPage);
