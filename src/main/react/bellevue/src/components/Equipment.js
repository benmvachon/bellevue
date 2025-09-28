import React, { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import PropTypes from 'prop-types';
import { getEquipment, equipItem, unequipItem } from '../api/api.js';
import Page from './Page.js';
import Modal from './Modal.js';
import Button from './Button.js';

function Equipment({ show = false, onClose, refreshProfile }) {
  const { pushAlert } = useOutletContext();
  const [slot, setSlot] = useState('all');
  const [equipment, setEquipment] = useState(null);
  const [error, setError] = useState(false);

  const refresh = () => getEquipment(setEquipment, setError, 0, slot || 'all');

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

  if (!show) return;

  return (
    <Modal className="equipment-container" show={show} onClose={onClose}>
      <div className="top-buttons">
        <Button onClick={() => setSlot('all')}>show all</Button>
      </div>
      <div className="equipment">
        <Page
          page={equipment}
          renderItem={(item) => (
            <div className="item" key={`item-${item.id}`}>
              {item.name}
              <Button onClick={() => setSlot(item.slot)}>{item.slot}</Button>
              {item.equipped ? (
                <Button onClick={() => unequip(item.id)}>unequip</Button>
              ) : (
                <Button onClick={() => equip(item.id)}>equip</Button>
              )}
            </div>
          )}
          loadPage={loadPage}
        />
      </div>
      <div className="buttons">
        <Button onClick={onClose}>close</Button>
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
