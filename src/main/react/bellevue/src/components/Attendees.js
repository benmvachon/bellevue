import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifyLocationChange } from '../utils/LocationContext.js';
import {
  getFriendsInLocation,
  onEntrance,
  unsubscribeLocation
} from '../api/api.js';

function Attendees() {
  const navigate = useNavigate();
  const { locationId, locationType } = useNotifyLocationChange();
  const [attendees, setAttendees] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (locationId && locationType) {
      onEntrance(() => getFriendsInLocation(setAttendees, setError));
      getFriendsInLocation(setAttendees, setError);
      return () => {
        unsubscribeLocation();
      };
    }
  }, [locationId, locationType]);

  if (error) return JSON.stringify(error);

  return (
    <div className="attendees">
      <h3>Attendees</h3>
      {attendees?.content?.map((attendee) => (
        <button
          key={`attendee-${attendee.id}`}
          onClick={() => navigate(`/profile/${attendee.id}`)}
        >
          {attendee.name}
        </button>
      ))}
    </div>
  );
}

Attendees.displayName = 'Attendees';

export default Attendees;
