CREATE TABLE user(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(150) NOT NULL,                                                                      -- Name of the user
    username        VARCHAR(150) NOT NULL UNIQUE,                                                               -- Username for the account
    password        VARCHAR(150) NOT NULL,                                                                      -- Password for the account
    status          ENUM('offline', 'online', 'cooking') NOT NULL DEFAULT 'offline',                            -- Indication of what the user is currently doing
    avatar          ENUM('cat', 'raptor', 'walrus', 'bee', 'monkey', 'horse') NOT NULL DEFAULT 'cat',           -- Image to represent the user
    hat             INT UNSIGNED,                                                                               -- The user's currently equipped hat accessory
    mask            INT UNSIGNED,                                                                               -- The user's currently equipped mask accessory
    shirt           INT UNSIGNED,                                                                               -- The user's currently equipped shirt accessory
    glow            INT UNSIGNED,                                                                               -- The user's currently equipped glow accessory
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the account was created
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the account was last updated
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (username)                                                                                  -- Index on the username for fast look-up
);
CREATE TABLE ingredient(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(150) NOT NULL UNIQUE,                                                               -- Name of the ingredient
    pescetarian     BIT DEFAULT 1,                                                                              -- Flag indicating pescetarian status
    vegetarian      BIT DEFAULT 1,                                                                              -- Flag indicating vegetarian status
    vegan           BIT DEFAULT 1,                                                                              -- Flag indicating vegan status
    gluten_free     BIT DEFAULT 1,                                                                              -- Flag indicating gluten-free status
    allergen        VARCHAR(150),                                                                               -- Catch-all for other allergens (shell-fish, nuts, etc.)
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (name)                                                                                      -- Index on the ingredient for fast look-up
);
CREATE TABLE skill(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(150) NOT NULL UNIQUE,                                                               -- Name of the skill
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (name)                                                                                      -- Index on the skill for fast look-up
);
CREATE TABLE equipment(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(150) NOT NULL UNIQUE,                                                               -- Name of the equipment
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (name)                                                                                      -- Index on the equipment for fast look-up
);
CREATE TABLE accessory(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    name            VARCHAR(150) NOT NULL UNIQUE,                                                               -- Name of the accessory
    slot            ENUM('hat', 'mask', 'shirt', 'glow') NOT NULL,                                              -- Slot on which the accessory is equipped
    criteria        VARCHAR(150) NOT NULL,                                                                      -- Criteria for unlocking the accessory
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    INDEX           (name)                                                                                      -- Index on the accessory for fast look-up
);
CREATE TABLE recipe(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    author          INT UNSIGNED NOT NULL,                                                                      -- ID of the user who added the recipe
    name            VARCHAR(150) NOT NULL UNIQUE,                                                               -- Name of the recipe
    description     TEXT,                                                                                       -- Description of the recipe
    category        ENUM('soup', 'salad', 'snack', 'side', 'main', 'dessert', 'cocktail', 'smoothie') NOT NULL, -- Description of the recipe
    pescetarian     BIT DEFAULT 1,                                                                              -- Flag indicating pescetarian status
    vegetarian      BIT DEFAULT 1,                                                                              -- Flag indicating vegetarian status
    vegan           BIT DEFAULT 1,                                                                              -- Flag indicating vegan status
    gluten_free     BIT DEFAULT 1,                                                                              -- Flag indicating gluten-free status
    allergen        VARCHAR(150),                                                                               -- Catch-all for other allergens (shell-fish, nuts, etc.)
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the recipe was created
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the recipe was last updated
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    FOREIGN KEY     (author) REFERENCES user(id) ON DELETE CASCADE,                                             -- author is a reference to the user table
    INDEX           (author)                                                                                    -- Index on the author for fast look-up
);
CREATE TABLE friend(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user
    friend          INT UNSIGNED NOT NULL,                                                                      -- ID of the friend
    status          ENUM('pending_them', 'pending_you', 'accepted', 'blocked_them', 'blocked_you') NOT NULL,    -- Friendship status
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the friendship was first established
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the friendship was updated
    PRIMARY KEY     (user, friend),                                                                             -- Composite primary key to ensure unique friendships
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (friend) REFERENCES user(id) ON DELETE CASCADE,                                             -- friend is a reference to the user table
    INDEX           (user)                                                                                      -- Index on the primary user for fast look-up
);
CREATE TABLE user_wardrobe(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user to whom the accessory is available
    accessory       INT UNSIGNED NOT NULL,                                                                      -- ID of the accessory
    PRIMARY KEY     (user, accessory),                                                                          -- The primary key is a combination of the two foreign keys
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (accessory) REFERENCES accessory(id) ON DELETE CASCADE,                                     -- accessory is a reference to the accessory table
    INDEX           (user)                                                                                      -- Index on the user for fast look-up
);
CREATE TABLE recipe_step(
    recipe          INT UNSIGNED NOT NULL,                                                                      -- ID of the recipe in which this step is included
    step            VARCHAR(150),                                                                               -- Description of the step in the process of cooking the recipe
    `order`         INT UNSIGNED NOT NULL,                                                                      -- When in the recipe this step should be performed
    PRIMARY KEY     (recipe, `order`),                                                                          -- The primary key is a combination of the recipe and the step order
    FOREIGN KEY     (recipe) REFERENCES recipe(id) ON DELETE CASCADE,                                           -- recipe is a reference to the recipe table
    INDEX           (recipe)                                                                                    -- Index on the recipe for fast look-up
);
CREATE TABLE recipe_ingredient(
    recipe          INT UNSIGNED NOT NULL,                                                                      -- ID of the recipe containing the ingredient
    ingredient      INT UNSIGNED NOT NULL,                                                                      -- ID of the ingredient contained in the recipe
    quantity        DECIMAL(10, 2) UNSIGNED NOT NULL,                                                           -- Number of units of the ingredient
    unit            VARCHAR(150),                                                                               -- Unit by which to measure the quanity of the ingredient
    PRIMARY KEY     (recipe, ingredient),                                                                       -- The primary key is a combination of the two foreign keys
    FOREIGN KEY     (recipe) REFERENCES recipe(id) ON DELETE CASCADE,                                           -- recipe is a reference to the recipe table
    FOREIGN KEY     (ingredient) REFERENCES ingredient(id) ON DELETE CASCADE,                                   -- ingredient is a reference to the ingredient table
    INDEX           (recipe),                                                                                   -- Index on the recipe for fast look-up
    INDEX           (ingredient)                                                                                -- Index on the ingredient for fast look-up
);
CREATE TABLE recipe_skill(
    recipe          INT UNSIGNED NOT NULL,                                                                      -- ID of the recipe containing the ingredient
    skill           INT UNSIGNED NOT NULL,                                                                      -- ID of the skill used in the recipe
    PRIMARY KEY     (recipe, skill),                                                                            -- The primary key is a combination of the two foreign keys
    FOREIGN KEY     (recipe) REFERENCES recipe(id) ON DELETE CASCADE,                                           -- recipe is a reference to the recipe table
    FOREIGN KEY     (skill) REFERENCES skill(id) ON DELETE CASCADE,                                             -- skill is a reference to the skill table
    INDEX           (recipe),                                                                                   -- Index on the recipe for fast look-up
    INDEX           (skill)                                                                                     -- Index on the skill for fast look-up
);
CREATE TABLE recipe_equipment(
    recipe          INT UNSIGNED NOT NULL,                                                                      -- ID of the recipe containing the ingredient
    equipment       INT UNSIGNED NOT NULL,                                                                      -- ID of the equipment used in the recipe
    PRIMARY KEY     (recipe, equipment),                                                                        -- The primary key is a combination of the two foreign keys
    FOREIGN KEY     (recipe) REFERENCES recipe(id) ON DELETE CASCADE,                                           -- recipe is a reference to the recipe table
    FOREIGN KEY     (equipment) REFERENCES equipment(id) ON DELETE CASCADE,                                     -- equipment is a reference to the equipment table
    INDEX           (recipe),                                                                                   -- Index on the recipe for fast look-up
    INDEX           (equipment)                                                                                 -- Index on the equipment for fast look-up
);
CREATE TABLE review(
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,                                                       -- Unique ID for the record
    recipe          INT UNSIGNED NOT NULL,                                                                      -- ID of the recipe being reviewed
    author          INT UNSIGNED NOT NULL,                                                                      -- ID of the user who wrote the review
    review          ENUM('disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly'),                -- Star rating out of five (prompted with "How did it go?")
    content         TEXT,                                                                                       -- Text content
    created         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the review was submitted
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the review was last updated
    PRIMARY KEY     (id),                                                                                       -- Make the ID the primary key
    FOREIGN KEY     (recipe) REFERENCES recipe(id) ON DELETE CASCADE,                                           -- recipe is a reference to the recipe table
    FOREIGN KEY     (author) REFERENCES user(id) ON DELETE CASCADE,                                             -- author is a reference to the user table
    INDEX           (recipe)                                                                                    -- Index on the recipe for fast look-up
);
CREATE TABLE aggregate_rating(
    user            INT UNSIGNED NOT NULL,                                                                      -- ID of the user for whom the aggregate rating applies
    recipe          INT UNSIGNED NOT NULL,                                                                      -- ID of the recipe containing the ingredient
    rating          DECIMAL(3,2),                                                                               -- The star rating aggregated from reviews visible to the user
    rating_count    INT UNSIGNED,                                                                               -- The number of reviews which were used to calculate the aggregate
    updated         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                                        -- Timestamp for when the aggregate was last updated
    PRIMARY KEY     (user, recipe),                                                                             -- The primary key is a combination of the two foreign keys
    FOREIGN KEY     (user) REFERENCES user(id) ON DELETE CASCADE,                                               -- user is a reference to the user table
    FOREIGN KEY     (recipe) REFERENCES recipe(id) ON DELETE CASCADE,                                           -- recipe is a reference to the recipe table
    INDEX           (user),                                                                                     -- Index on the user for fast look-up
    INDEX           (recipe)                                                                                    -- Index on the recipe for fast look-up
);
CREATE VIEW scrubbed_user AS SELECT id, name, username, status, avatar FROM user;                             -- Scrubbed version of the user table to secure sensitive data
CREATE VIEW simple_recipe AS SELECT id, author, name, category, created, updated FROM recipe;                 -- Simplified version of the recipe for list views and join connections
