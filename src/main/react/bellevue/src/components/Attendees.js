import { useEffect, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useNotifyLocationChange } from '../utils/LocationContext.js';
import {
  getFriendsInLocation,
  onEntrance,
  unsubscribeLocation
} from '../api/api.js';
import Button from './Button.js';

function Attendees() {
  const navigate = useNavigate();
  const { pushAlert } = useOutletContext();
  const { locationId, locationType } = useNotifyLocationChange();
  const [attendees, setAttendees] = useState(null);
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
    if (locationId && locationType) {
      onEntrance(() => getFriendsInLocation(setAttendees, setError));
      getFriendsInLocation(setAttendees, setError);
      return () => {
        unsubscribeLocation();
      };
    }
  }, [locationId, locationType]);

  return (
    <div className="attendees">
      <h3>Attendees</h3>
      {attendees?.content?.map((attendee) => (
        <Button
          key={`attendee-${attendee.id}`}
          onClick={() => navigate(`/home/${attendee.id}`)}
        >
          {attendee.name}
        </Button>
      ))}
    </div>
  );
}

Attendees.displayName = 'Attendees';

export default Attendees;
