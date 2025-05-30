import { useEffect, useState } from 'react';
import { getForum, getForums, markPostsRead } from '../api/api.js';
import Page from '../components/Page.js';
import Forum from '../components/Forum.js';

function ForumsMap() {
  const [forums, setForums] = useState(undefined);
  const [filter, setFilter] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadForumPage = (page) => {
    setLoading(true);
    getForums(
      (forums) => {
        setForums(forums);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      page,
      filter
    );
  };

  useEffect(() => {
    setLoading(true);
    getForums(
      (forums) => {
        setForums(forums);
        setLoading(false);
      },
      (error) => {
        setError(error);
        setLoading(false);
      },
      0,
      filter
    );
  }, [filter]);

  useEffect(() => {
    if (
      forums &&
      forums.content &&
      !forums.content.find((forum) => forum.id === 1)
    ) {
      setLoading(true);
      getForum(
        1,
        (forum) => {
          forums.content.push(forum);
          setForums({ ...forums });
          setLoading(false);
        },
        setError
      );
    }
  }, [forums]);

  if (error) return JSON.stringify(error);

  return (
    <div className="forums">
      <h2>
        Forums
        <button onClick={() => setFilter(!filter)}>
          {filter ? 'Show all' : 'Show unread'}
        </button>
        <button onClick={markPostsRead}>Mark all as read</button>
      </h2>
      <div>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <Page
            page={forums}
            renderItem={(forum) => <Forum id={forum.id} forumProp={forum} />}
            loadPage={loadForumPage}
          />
        )}
      </div>
    </div>
  );
}

ForumsMap.displayName = 'ForumsMap';

export default ForumsMap;
