import React from 'react';
import PropTypes from 'prop-types';
import Modal from './Modal.js';
import Button from './Button.js';

function ConfirmationDialog({
  show = false,
  description = 'are you sure?',
  confirmText = 'confirm',
  cancelText = 'cancel',
  onConfirm,
  onCancel
}) {
  return (
    <Modal
      className="confirmation-dialog pixel-corners"
      show={show}
      onClose={onCancel}
    >
      <p>{description}</p>
      <div className="buttons">
        <Button className="confirm" onClick={onConfirm}>
          {confirmText}
        </Button>
        <Button className="cancel" onClick={onCancel}>
          {cancelText}
        </Button>
      </div>
    </Modal>
  );
}

ConfirmationDialog.propTypes = {
  show: PropTypes.bool,
  description: PropTypes.string,
  confirmText: PropTypes.string,
  cancelText: PropTypes.string,
  destructive: PropTypes.bool,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired
};

ConfirmationDialog.displayName = 'ConfirmationDialog';

export default ConfirmationDialog;
