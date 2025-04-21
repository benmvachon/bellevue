import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import withAuth from '../utils/withAuth.js';
import { getEquipment, equipItem, unequipItem } from '../api/api.js';
import InfiniteScroll from './InfiniteScroll.js';

function Equipment({ show = false, onClose, refreshProfile }) {
  const [slot, setSlot] = useState('all');
  const [equipment, setEquipment] = useState(null);
  const [error, setError] = useState(false);

  const refresh = () => getEquipment(setEquipment, setError, 0, slot || 'all');

  useEffect(() => {
    if (show) refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show, slot]);

  const loadMore = (page) => {
    getEquipment(
      (more) => {
        if (more) {
          more.content = equipment?.content?.concat(more?.content);
          more.number = more.number + equipment?.number || 0;
          setEquipment(more);
        }
      },
      setError,
      page,
      slot
    );
  };

  const equip = (item) => {
    equipItem(
      item,
      () => {
        refresh();
        refreshProfile();
      },
      setError
    );
  };

  const unequip = (item) => {
    unequipItem(
      item,
      () => {
        refresh();
        refreshProfile();
      },
      setError
    );
  };

  if (error) return JSON.stringify(error);
  if (!show) return;

  return (
    <div className="modal-container">
      <div className="modal threads-container">
        <div className="top-buttons">
          <button onClick={() => setSlot('all')}>Show all</button>
        </div>
        <div className="threads">
          <InfiniteScroll
            page={equipment}
            renderItem={(item) => (
              <div className="thread" key={`thread-${item.id}`}>
                {item.name}
                <button onClick={() => setSlot(item.slot)}>{item.slot}</button>
                {item.equipped ? (
                  <button onClick={() => unequip(item.id)}>unequip</button>
                ) : (
                  <button onClick={() => equip(item.id)}>equip</button>
                )}
              </div>
            )}
            loadMore={loadMore}
          />
        </div>
        <div className="buttons">
          <button onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}

Equipment.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  refreshProfile: PropTypes.func.isRequired
};

export default withAuth(Equipment);
