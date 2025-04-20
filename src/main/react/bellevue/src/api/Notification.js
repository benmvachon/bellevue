import Profile from './Profile';

class Notification {
  constructor(id, notifier, notified, type, entity, read, created, _links) {
    this.id = id;
    this.notifier = notifier ? Profile.fromJSON(notifier) : undefined;
    this.notified = notified;
    this.typeName = type?.name;
    this.typeId = type?.typeId;
    this.entity = entity;
    this.read = read;
    this.created = created ? new Date(created) : undefined;
    // TODO: process _links
  }

  static notificationMapper = {
    fromPage(_embedded) {
      const notifications = [];
      if (_embedded) {
        for (const notification of _embedded.notificationEntityList) {
          notifications.push(Notification.fromJSON(notification));
        }
      }
      return notifications;
    }
  };

  static fromJSON(json) {
    return new Notification(
      json.id,
      json.notifier,
      json.notified,
      json.type,
      json.entity,
      json.read,
      json.created
    );
  }

  toJSON() {
    return {
      id: this.id,
      notifier: this.notifier ? this.notifier.toJSON() : null,
      notified: this.notified,
      type: { name: this.typeName, id: this.typeId },
      entity: this.entity,
      read: this.read,
      created: this.created?.toString()
    };
  }
}

export default Notification;
