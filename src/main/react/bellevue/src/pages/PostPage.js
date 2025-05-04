import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { useNotifyLocationChange } from '../utils/LocationContext.js';
import { getPost, getFriendsInLocation, onEntrance } from '../api/api.js';
import Post from '../components/Post.js';
import Header from '../components/Header.js';
import InfiniteScroll from '../components/InfiniteScroll.js';

function PostPage() {
  const navigate = useNavigate();
  const { locationId, locationType } = useNotifyLocationChange();
  const { id } = useParams();
  const [post, setPost] = useState(null);
  const [attendees, setAttendees] = useState(null);
  const [error, setError] = useState(false);

  const refreshPost = () => getPost(id, setPost, setError);
  const refreshAttendees = () => getFriendsInLocation(setAttendees, setError);

  const loadMoreAttendees = (page) => {
    getFriendsInLocation(
      id,
      'POST',
      (more) => {
        if (more) {
          more.content = attendees?.content?.concat(more?.content);
          setAttendees(more);
        }
      },
      setError,
      page
    );
  };

  useEffect(() => {
    if (id) {
      refreshPost();
      refreshAttendees();
      onEntrance(refreshAttendees);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    refreshAttendees();
  }, [locationId, locationType]);

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
        <div className="attendees">
          <h3>Attendees</h3>
          <InfiniteScroll
            page={attendees}
            renderItem={(attendee) => (
              <button onClick={() => navigate(`/profile/${attendee.id}`)}>
                {attendee.name}
              </button>
            )}
            loadMore={loadMoreAttendees}
          />
        </div>
        <div className="posts">
          <h3>Posts</h3>
          {i > 0 ? parentFuncs[i - 1](0, true) : getPostElement(0, true)}
        </div>
      </div>
    </div>
  );
}

export default withAuth(PostPage);
