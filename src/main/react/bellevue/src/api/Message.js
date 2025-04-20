import Profile from './Profile';

class Message {
  constructor(id, sender, receiver, message, read, created, _links) {
    this.id = id;
    this.sender = sender ? Profile.fromJSON(sender) : null;
    this.receiver = receiver ? Profile.fromJSON(receiver) : null;
    this.message = message;
    this.read = read;
    this.created = new Date(created);
    // TODO: process _links
  }

  static messageMapper = {
    fromPage(_embedded) {
      const messages = [];
      if (_embedded) {
        for (const message of _embedded.messageEntityList) {
          messages.push(Message.fromJSON(message));
        }
      }
      return messages;
    }
  };

  static fromJSON(json) {
    return new Message(
      json.id,
      json.sender,
      json.receiver,
      json.message,
      json.read,
      json.created
    );
  }

  toJSON() {
    return {
      id: this.id,
      sender: this.sender ? this.sender.toJSON() : null,
      receiver: this.receiver ? this.receiver.toJSON() : null,
      message: this.message,
      read: this.read,
      created: this.created?.toString()
    };
  }
}

export default Message;
