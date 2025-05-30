CREATE TABLE user(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(255) NOT NULL,                                                                      -- Name of the user
    username        VARCHAR(255) NOT NULL UNIQUE,                                                               -- Username for the account
    password        BINARY(60) NOT NULL,                                                                        -- Password for the account
    email           VARCHAR(255) NOT NULL UNIQUE,                                                               -- Email for the account
    verified        BOOLEAN NOT NULL DEFAULT 0,                                                                 -- Flag indicating verification status
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the account was created
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the account was last updated
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (username),                                                                                 -- Index on the username for fast look-up
    INDEX           (email)                                                                                     -- Index on the email for fast look-up
);
CREATE TABLE avatar(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(255) NOT NULL UNIQUE,                                                               -- Name of the avatar (cat, raptor, walrus, bee, monkey, horse, etc)
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (name)                                                                                      -- Index on the name for fast look-up
);
CREATE TABLE forum(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(255) NOT NULL UNIQUE,                                                               -- Name of the forum
    description     VARCHAR(255),                                                                               -- Forum's purpose
    user            INT UNSIGNED,                                                                               -- Forum's creator (if this is a custom forum)
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the account was created
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    INDEX           (name),                                                                                     -- Index on the name for fast look-up
    INDEX           (user)                                                                                      -- Index on the user for fast look-up
);
CREATE TABLE forum_tag(
    forum           INT UNSIGNED NOT NULL,                                                                      -- Forum to which the tag applies
    tag             VARCHAR(255) NOT NULL,                                                                      -- Tag for filtering and finding forums
    PRIMARY KEY     (forum, tag),                                                                               -- Composite primary key to ensure m2m
    FOREIGN KEY     (forum) REFERENCES forum(id) ON DELETE CASCADE,                                             -- forum is a reference to the forum table
    INDEX           (tag)                                                                                       -- Index on the tag for fast look-up
);
CREATE TABLE forum_security(
    forum           INT UNSIGNED NOT NULL,                                                                      -- Forum to which the security applies
    user            INT UNSIGNED NOT NULL,                                                                      -- User permitted to view forum
    PRIMARY KEY     (forum, user),                                                                               -- Composite primary key to ensure m2m
    FOREIGN KEY     (forum) REFERENCES forum(id) ON DELETE CASCADE,                                             -- forum is a reference to the forum table
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    INDEX           (forum),                                                                                    -- Index on the forum for fast look-up
    INDEX           (user)                                                                                      -- Index on the user for fast look-up
);
CREATE TABLE notification_setting(
    user            INT UNSIGNED NOT NULL,                                                                      -- User to whom the setting applies
    forum           INT UNSIGNED NOT NULL,                                                                      -- Forum to which the setting applies
    notify          BOOLEAN NOT NULL DEFAULT 0,                                                                 -- Flag indicating whether or not to notify the user
    PRIMARY KEY     (user, forum),                                                                              -- Composite primary key to ensure unique settings
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (forum) REFERENCES forum(id) ON DELETE CASCADE,                                             -- forum is a reference to the forum table
    INDEX           (user),                                                                                     -- Index on the user for fast look-up
    INDEX           (forum)                                                                                     -- Index on the forum for fast look-up
);
CREATE TABLE profile(
    user            INT UNSIGNED NOT NULL UNIQUE,                                                               -- ID of the user
    status          ENUM('OFFLINE', 'ACTIVE', 'IDLE', 'OTHER') NOT NULL DEFAULT 'OFFLINE',                      -- Indication of what the user is currently doing
    location        INT UNSIGNED,                                                                               -- The ID of the forum / profile the user is currently viewing
    location_type   ENUM('FORUM', 'PROFILE', 'POST', 'OTHER'),                                                  -- The name of the table / entity of the location
    last_seen       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the user last logged out
    blackboard      VARCHAR(255),                                                                               -- Custom message written by the user
    avatar          INT UNSIGNED NOT NULL DEFAULT 0,                                                            -- Image to represent the user
    PRIMARY KEY     (user),                                                                                     -- Make the ID the primary key
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (avatar) REFERENCES avatar(id),                                                             -- avatar is a reference to the avatar table
    INDEX           (location)
);
CREATE TABLE item(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(255) NOT NULL UNIQUE,                                                               -- Name of the item
    slot            VARCHAR(255) NOT NULL,                                                                      -- Where the item is equipped (avatar, hat, mask, shirt, glow, etc)
    starter         BOOLEAN NOT NULL DEFAULT 0,                                                                 -- Flag indicating starter-item status
    unlockable      BOOLEAN NOT NULL DEFAULT 1,                                                                 -- Flag indicating unlockable status
    PRIMARY KEY     (id),                                                                                       -- Joint primary key of user and item
    INDEX           (name),                                                                                     -- Index on the slot for fast look-up
    INDEX           (slot)                                                                                      -- Index on the name for fast look-up
);
CREATE TABLE equipment(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user
    item            INT UNSIGNED NOT NULL,                                                                      -- Name of the equipment
    equipped        BOOLEAN NOT NULL DEFAULT 0,                                                                 -- Flag indicating equipment status
    unlocked        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the item was unlocked by the user
    PRIMARY KEY     (user, item),                                                                               -- Joint primary key of user and item
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (item) REFERENCES item(id) ON DELETE CASCADE,                                               -- item is a reference to the item table
    INDEX           (user),                                                                                     -- Index on the user for fast look-up
    INDEX           (item),                                                                                     -- Index on the item for fast look-up
    INDEX           (equipped),                                                                                 -- Index on the equipped status for fast look-up
    INDEX           (unlocked)                                                                                  -- Index on the unlocked date for fast look-up
);
CREATE TABLE friend(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user
    friend          INT UNSIGNED NOT NULL,                                                                      -- ID of the friend
    status          ENUM('PENDING_THEM', 'PENDING_YOU', 'ACCEPTED', 'BLOCKED_THEM', 'BLOCKED_YOU') NOT NULL,    -- Friendship status
    score           INT UNSIGNED NOT NULL DEFAULT 0,                                                            -- How close the friend is
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the friendship was first established
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the friendship was updated
    PRIMARY KEY     (user, friend),                                                                             -- Composite primary key to ensure unique friendships
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (friend) REFERENCES user(id) ON DELETE CASCADE,                                             -- friend is a reference to the user table
    INDEX           (user)                                                                                      -- Index on the primary user for fast look-up
);
CREATE TABLE post(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user
    parent          INT UNSIGNED,                                                                               -- Parent post (if this is a comment)
    forum           INT UNSIGNED NOT NULL,                                                                      -- Forum containing the post
    content         TEXT NOT NULL,                                                                              -- Text content of the post / comment
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the post was created
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,                                                             -- Whether the post has been marked for deletion
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (parent) REFERENCES post(id) ON DELETE CASCADE,                                             -- parent is a reference to the post table
    FOREIGN KEY     (forum) REFERENCES forum(id) ON DELETE CASCADE,                                             -- forum is a reference to the forum table
    INDEX           (user),                                                                                     -- Index on the user for fast look-up
    INDEX           (parent),                                                                                   -- Index on the parent for fast look-up
    INDEX           (forum),                                                                                    -- Index on the forum for fast look-up
    INDEX           (user, parent),                                                                             -- Index on the user & parent for fast look-up
    INDEX           (user, forum)                                                                               -- Index on the user & forum for fast look-up
);
CREATE TABLE rating(
    post            INT UNSIGNED NOT NULL,                                                                      -- ID of the post being rated
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user who added the rating
    rating          ENUM('ONE', 'TWO', 'THREE', 'FOUR', 'FIVE', 'OTHER') NOT NULL DEFAULT 'FIVE',               -- Star rating out of five
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the rating was submitted
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the rating was last updated
    PRIMARY KEY     (user, post),                                                                               -- Make the ID a combination of user and post
    FOREIGN KEY     (post) REFERENCES post(id) ON DELETE CASCADE,                                               -- post is a reference to the post table
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    INDEX           (post)                                                                                      -- Index on the post for fast look-up
);
CREATE TABLE aggregate_rating(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user for whom the aggregate rating applies
    post            INT UNSIGNED NOT NULL,                                                                      -- ID of the post being rated
    rating          DECIMAL(3,2),                                                                               -- The star rating aggregated from ratings visible to the user
    rating_count    INT UNSIGNED,                                                                               -- The number of ratings which were used to calculate the aggregate
    popularity      INT UNSIGNED,                                                                               -- The number of ratings plus the popularity value of all child posts
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the aggregate was last updated
    `read`          BOOLEAN NOT NULL DEFAULT FALSE,                                                             -- Flag indicating whether the post has been read or not
    PRIMARY KEY     (user, post),                                                                               -- The primary key is a combination of the two foreign keys
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (post) REFERENCES post(id) ON DELETE CASCADE,                                               -- post is a reference to the post table
    INDEX           (user),                                                                                     -- Index on the user for fast look-up
    INDEX           (post)                                                                                      -- Index on the post for fast look-up
);
CREATE TABLE message(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    sender          INT UNSIGNED NOT NULL,                                                                      -- ID of the user who sent the message
    receiver        INT UNSIGNED NOT NULL,                                                                      -- ID of the user to whom the message was sent
    message         TEXT NOT NULL,                                                                              -- Text content of the message
    `read`          BOOLEAN NOT NULL DEFAULT FALSE,                                                             -- Flag indicating whether the message has been read or not
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the aggregate was last updated
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    FOREIGN KEY     (sender) REFERENCES user(id) ON DELETE CASCADE,                                             -- sender is a reference to the user table
    FOREIGN KEY     (receiver) REFERENCES user(id) ON DELETE CASCADE,                                           -- receiver is a reference to the user table
    INDEX           (sender),                                                                                   -- Index on the sender for fast look-up
    INDEX           (receiver),                                                                                 -- Index on the receiver for fast look-up
    INDEX           (created),                                                                                  -- Index on the created date for fast look-up
    INDEX           (sender, receiver)                                                                          -- Index on the user pair for fast look-up
);
CREATE TABLE notification(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    notified        INT UNSIGNED NOT NULL,                                                                      -- ID of the user who receives the notification
    notifier        INT UNSIGNED NOT NULL,                                                                      -- ID of the user causing the notification
    type            ENUM(
        'FORUM', 'POST', 'REPLY', 'RATING', 'REQUEST', 'ACCEPTANCE', 'MESSAGE', 'EQUIPMENT', 'OTHER'
    ) NOT NULL DEFAULT 'OTHER',                                                                                 -- Type of notification (used to get the record referenced by the entity)
    entity          INT UNSIGNED,                                                                               -- ID of the object (post, request, rating) which caused the notification
    `read`          BOOLEAN NOT NULL DEFAULT 0,                                                                 -- Flag indicating whether the message has been read or not
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the aggregate was last updated
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    FOREIGN KEY     (notifier) REFERENCES user(id) ON DELETE CASCADE,                                           -- notifier is a reference to the user table
    FOREIGN KEY     (notifier) REFERENCES user(id) ON DELETE CASCADE,                                           -- notified is a reference to the user table
    INDEX           (notifier),                                                                                 -- Index on the notifier for fast look-up
    INDEX           (notified),                                                                                 -- Index on the notified for fast look-up
    INDEX           (created),                                                                                  -- Index on the created date for fast look-up
    INDEX           (notifier, notified)                                                                        -- Index on the user pair for fast look-up
);
CREATE TABLE favorite(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user for whom the aggregate rating applies
    type            ENUM('POST', 'FORUM', 'PROFILE', 'OTHER') NOT NULL,                                         -- The name of the table / entity which is favorited
    entity          INT UNSIGNED NOT NULL,                                                                      -- ID of the object (post, request, rating) which is favorited
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the item was favorited
    PRIMARY KEY     (user, type, entity),                                                                       -- Make a composite key of three fields
    INDEX           (user),                                                                                     -- Index on the user for fast look-up
    INDEX           (type),                                                                                     -- Index on the type for fast look-up
    INDEX           (created)                                                                                   -- Index on the created date for fast look-up
);
CREATE VIEW user_profile AS
SELECT
    p.user,
    u.name,
    u.username,
    p.status,
    p.location,
    p.location_type,
    p.last_seen,
    p.blackboard,
    COALESCE(a.name, 'cat') AS avatar,
    -- Only aggregate equipment if slot is not null
    JSON_OBJECTAGG(
        COALESCE(i.slot, 'null'),
        COALESCE(i.name, 'null')
    ) AS equipment
FROM profile p
JOIN user u ON p.user = u.id
LEFT JOIN avatar a ON p.avatar = a.id
LEFT JOIN equipment e ON p.user = e.user AND e.equipped = 1
LEFT JOIN item i ON e.item = i.id
GROUP BY
    p.user, u.name, u.username,
    p.status, p.location, p.last_seen,
    p.blackboard, a.name;
