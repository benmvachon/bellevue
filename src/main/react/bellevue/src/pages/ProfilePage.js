import { useState } from 'react';
import { useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getFriend } from '../api/api.js';

function ProfilePage() {
  const { id } = useParams();
  const [profile, setProfile] = useState('');
  getFriend(id)
    .then((response) => {
      setProfile(JSON.stringify(response));
    })
    .catch((err) => {
      console.log(err);
    });

  return (
    <div className="page profile-page">
      <h1>Profile</h1>
      <div>{profile}</div>
    </div>
  );
}

export default withAuth(ProfilePage);
