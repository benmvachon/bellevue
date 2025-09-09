import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { getEquipment, equipItem, unequipItem } from '../api/api.js';
import Page from './Page.js';
import Modal from './Modal.js';

function Equipment({ show = false, onClose, refreshProfile }) {
  const [slot, setSlot] = useState('all');
  const [equipment, setEquipment] = useState(null);
  const [error, setError] = useState(false);

  const refresh = () => getEquipment(setEquipment, setError, 0, slot || 'all');

  useEffect(() => {
    if (show) refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show, slot]);

  const loadPage = (page) => {
    getEquipment(setEquipment, setError, page, slot);
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
    <Modal className="equipment-container" show={show} onClose={onClose}>
      <div className="top-buttons">
        <button onClick={() => setSlot('all')}>show all</button>
      </div>
      <div className="equipment">
        <Page
          page={equipment}
          renderItem={(item) => (
            <div className="item" key={`item-${item.id}`}>
              {item.name}
              <button onClick={() => setSlot(item.slot)}>{item.slot}</button>
              {item.equipped ? (
                <button onClick={() => unequip(item.id)}>unequip</button>
              ) : (
                <button onClick={() => equip(item.id)}>equip</button>
              )}
            </div>
          )}
          loadPage={loadPage}
        />
      </div>
      <div className="buttons">
        <button onClick={onClose}>close</button>
      </div>
    </Modal>
  );
}

Equipment.propTypes = {
  show: PropTypes.bool,
  onClose: PropTypes.func.isRequired,
  refreshProfile: PropTypes.func.isRequired
};

Equipment.displayName = 'Equipment';

export default Equipment;
