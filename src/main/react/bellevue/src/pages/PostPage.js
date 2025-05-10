import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getPost } from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';
import Attendees from '../components/Attendees.js';

function PostPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [post, setPost] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (id) getPost(id, setPost, setError);
  }, [id]);

  if (error) return JSON.stringify(error);
  if (!post) return;

  const getPostElement = (depth) => (
    <Post id={post.id} depth={depth} selected />
  );
  let parent = post.parent;
  let previousParent = post;
  const parentFuncs = [];
  let i = 0;
  while (parent) {
    const id = Number.parseInt('' + parent.id);
    const previousId = Number.parseInt('' + previousParent.id);
    const pointer = Number.parseInt('' + i);
    parentFuncs[pointer] = (depth) => (
      <Post
        id={id}
        depth={depth}
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
        <Attendees />
        <div className="posts">
          <h3>Posts</h3>
          {i > 0 ? parentFuncs[i - 1](0, true) : getPostElement(0, true)}
        </div>
      </div>
    </div>
  );
}

export default withAuth(PostPage);
