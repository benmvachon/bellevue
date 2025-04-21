DELIMITER //
CREATE PROCEDURE add_user(
    IN p_name VARCHAR(255),
    IN p_username VARCHAR(255),
    IN p_email VARCHAR(255),
    IN p_password BINARY(60),
    OUT p_user_id INT UNSIGNED,
    OUT p_avatar_name VARCHAR(255),
    OUT p_hat_name VARCHAR(255)
)
BEGIN
    DECLARE v_avatar_id INT UNSIGNED;
    DECLARE v_hat_id INT UNSIGNED;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_user_id = NULL;
        SET p_avatar_name = NULL;
        ROLLBACK;
    END;

    START TRANSACTION;

    -- Insert into the user table
    INSERT INTO user (name, username, password, email)
    VALUES (p_name, p_username, p_password, p_email);

    -- Get the last inserted id
    SET p_user_id = LAST_INSERT_ID();

    -- Pick a random avatar ID and name
    SELECT id, name INTO v_avatar_id, p_avatar_name
    FROM avatar
    ORDER BY RAND()
    LIMIT 1;

    -- Insert into the profile table
    INSERT INTO profile (user, avatar)
    VALUES (p_user_id, v_avatar_id);

    -- Pick a random hat ID and name
    SELECT id, name INTO v_hat_id, p_hat_name
    FROM item
    WHERE id < 8
    ORDER BY RAND()
    LIMIT 1;

    INSERT INTO equipment (user, item, equipped)
    VALUES (p_user_id, v_hat_id, true);

    COMMIT;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_rating_to_aggregate(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_rating DECIMAL(3,2)
)
BEGIN
    DECLARE stored_rating DECIMAL(3,2);
    DECLARE stored_count INT UNSIGNED;
    -- Check if the aggregate already exists
    IF NOT EXISTS (
        SELECT * FROM aggregate_rating
        WHERE user = p_user AND post = p_post
    ) THEN
        -- Add it if it doesn't
        INSERT INTO aggregate_rating (user, post, rating, rating_count)
        VALUES (p_user, p_post, p_rating, 1);
    ELSE
        -- Update it if it does
        SELECT rating, rating_count INTO stored_rating, stored_count
        FROM aggregate_rating
        WHERE user = p_user AND post = p_post;
        SET stored_count = stored_count + 1;
        SET stored_rating = (stored_rating * (stored_count - 1) + p_rating) / stored_count;
        UPDATE aggregate_rating 
        SET rating = stored_rating, rating_count = stored_count, updated = CURRENT_TIMESTAMP
        WHERE user = p_user AND post = p_post;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE update_rating_in_aggregate(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_new_rating DECIMAL(3,2),
    IN p_old_rating DECIMAL(3,2)
)
BEGIN
    DECLARE stored_rating DECIMAL(3,2);
    DECLARE stored_count INT UNSIGNED;
    -- Check if the aggregate already exists (it really should because this was triggered by an update)
    IF NOT EXISTS (
        SELECT * FROM aggregate_rating
        WHERE user = p_user AND post = p_post
    ) THEN
    -- Add it if it doesn't
        INSERT INTO aggregate_rating (user, post, rating, rating_count)
        VALUES (p_user, p_post, p_new_rating, 1);
    ELSE
    -- Update it if it does
        SELECT rating, rating_count INTO stored_rating, stored_count
        FROM aggregate_rating
        WHERE user = p_user AND post = p_post;
        SET stored_rating = (stored_rating * stored_count - p_old_rating + p_new_rating) / stored_count;
        UPDATE aggregate_rating 
        SET rating = stored_rating,updated = CURRENT_TIMESTAMP
        WHERE user = p_user AND post = p_post;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE remove_rating_from_aggregate(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_rating DECIMAL(3,2)
)
BEGIN
    DECLARE stored_rating DECIMAL(3,2);
    DECLARE stored_count INT UNSIGNED;
    -- Check if the aggregate exists
    IF EXISTS (
        SELECT * FROM aggregate_rating
        WHERE user = p_user AND post = p_post
    ) THEN
        -- Retrieve existing rating and count
        SELECT rating, rating_count INTO stored_rating, stored_count
        FROM aggregate_rating
        WHERE user = p_user AND post = p_post;
        -- Check if this is the last rating
        IF stored_count = 1 THEN
            -- If this is the only rating, delete the aggregate entry
            DELETE FROM aggregate_rating
            WHERE user = p_user AND post = p_post;
        ELSE
            -- Calculate new average rating after deletion
            SET stored_rating = (stored_rating * stored_count - p_rating) / (stored_count - 1);
            SET stored_count = stored_count - 1;
            -- Update the aggregate rating with the new average
            UPDATE aggregate_rating 
            SET rating = stored_rating, rating_count = stored_count, updated = CURRENT_TIMESTAMP
            WHERE user = p_user AND post = p_post;
        END IF;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE accept_friend(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE rating_user INT UNSIGNED;
    DECLARE rating_post INT UNSIGNED;
    DECLARE rating_rating DECIMAL(3,2);
    -- Cursor for ratings usered by p_user
    DECLARE rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'one', 'two', 'three', 'four', 'five') AS rating FROM rating WHERE user = p_user;
    -- Cursor for ratings usered by p_friend
    DECLARE rating_cursor_friend CURSOR FOR 
        SELECT post, FIELD(rating, 'one', 'two', 'three', 'four', 'five') AS rating FROM rating WHERE user = p_friend;
    -- Handler to exit the loop
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Check if the friendship already exists with pending status
    IF EXISTS (
        SELECT * FROM friend
        WHERE user = p_user AND friend = p_friend AND status = 'pending_you'
    ) THEN
        -- Update it if it does
        UPDATE friend 
        SET status = 'accepted', updated = CURRENT_TIMESTAMP 
        WHERE user = p_user AND friend = p_friend;
        -- Check if the reciprocal friendship already exists
        IF NOT EXISTS (
            SELECT * FROM friend
            WHERE user = p_friend AND friend = p_user
        ) THEN
            -- Add it if it doesn't
            INSERT INTO friend (user, friend, status)
            VALUES (p_friend, p_user, 'accepted');
        ELSE
            -- Update it if it does
            UPDATE friend 
            SET status = 'accepted', updated = CURRENT_TIMESTAMP 
            WHERE user = p_friend AND friend = p_user;
        END IF;
        -- Process ratings usered by p_user
        OPEN rating_cursor;
        read_loop: LOOP
            FETCH rating_cursor INTO rating_post, rating_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL add_rating_to_aggregate(p_friend, rating_post, rating_rating);
        END LOOP;
        CLOSE rating_cursor;
        -- Reset done flag for the second cursor
        SET done = FALSE;
        -- Process ratings usered by p_friend
        OPEN rating_cursor_friend;
        read_loop_friend: LOOP
            FETCH rating_cursor_friend INTO rating_post, rating_rating;
            IF done THEN
                LEAVE read_loop_friend;
            END IF;
            CALL add_rating_to_aggregate(p_user, rating_post, rating_rating);
        END LOOP;
        CLOSE rating_cursor_friend;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE request_friend(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN
    -- Insert the friendship record with status 'pending_them' to facilitate acceptance
    INSERT INTO friend (user, friend, status)
    VALUES (p_user, p_friend, 'pending_them')
    ON DUPLICATE KEY UPDATE status = 'pending_them', updated = CURRENT_TIMESTAMP;
    -- Insert the reciprocal friendship record with status 'pending_you' to facilitate acceptance
    INSERT INTO friend (user, friend, status)
    VALUES (p_friend, p_user, 'pending_you')
    ON DUPLICATE KEY UPDATE 
        status = 'pending_you', updated = CURRENT_TIMESTAMP;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE block_friend(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN
    DECLARE rating_post INT UNSIGNED;
    DECLARE rating_rating DECIMAL(3,2);
    DECLARE done INT DEFAULT FALSE;
    -- Declare the cursors and handlers at the start
    DECLARE friend_rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'one', 'two', 'three', 'four', 'five') AS rating FROM rating WHERE user = p_friend;
    DECLARE user_rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'one', 'two', 'three', 'four', 'five') AS rating FROM rating WHERE user = p_user;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Check if the friendship already exists
    IF EXISTS (
        SELECT * FROM friend
        WHERE user = p_user AND friend = p_friend
    ) THEN
        -- Update the status to 'blocked'
        UPDATE friend 
        SET status = 'blocked_them', updated = CURRENT_TIMESTAMP 
        WHERE user = p_user AND friend = p_friend;
        -- Process ratings usered by the friend
        OPEN friend_rating_cursor;
        read_loop: LOOP
            FETCH friend_rating_cursor INTO rating_post, rating_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL remove_rating_from_aggregate(p_user, rating_post, rating_rating);
        END LOOP;
        CLOSE friend_rating_cursor;
        -- Process ratings usered by the user
        OPEN user_rating_cursor;
        read_loop: LOOP
            FETCH user_rating_cursor INTO rating_post, rating_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL remove_rating_from_aggregate(p_friend, rating_post, rating_rating);
        END LOOP;
        CLOSE user_rating_cursor;
    ELSE
        -- If the friendship does not exist, just insert a blocked entry
        INSERT INTO friend (user, friend, status)
        VALUES (p_user, p_friend, 'blocked_them');
    END IF;
    -- Insert the reciprocal friendship record with status 'blocked_you' to prevent visibility
    INSERT INTO friend (user, friend, status)
    VALUES (p_friend, p_user, 'blocked_you')
    ON DUPLICATE KEY UPDATE 
        status = 'blocked_you', updated = CURRENT_TIMESTAMP;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE remove_friend(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN
    -- Remove ratings from the aggregate for both users
    DECLARE done INT DEFAULT FALSE;
    DECLARE rating_post INT UNSIGNED;
    DECLARE rating_rating DECIMAL(3,2);
    -- Cursor for ratings usered by p_user
    DECLARE rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'one', 'two', 'three', 'four', 'five') AS rating FROM rating WHERE user = p_user;
    -- Cursor for ratings usered by p_friend
    DECLARE rating_cursor_friend CURSOR FOR 
        SELECT post, FIELD(rating, 'one', 'two', 'three', 'four', 'five') AS rating FROM rating WHERE user = p_friend;
    -- Handler to exit the loop
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Process ratings usered by p_user
    OPEN rating_cursor;
    read_loop: LOOP
        FETCH rating_cursor INTO rating_post, rating_rating;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL remove_rating_from_aggregate(p_friend, rating_post, rating_rating);
    END LOOP;
    CLOSE rating_cursor;
    -- Reset done flag for the second cursor
    SET done = FALSE;
    -- Process ratings usered by p_friend
    OPEN rating_cursor_friend;
    read_loop_friend: LOOP
        FETCH rating_cursor_friend INTO rating_post, rating_rating;
        IF done THEN
            LEAVE read_loop_friend;
        END IF;
        CALL remove_rating_from_aggregate(p_user, rating_post, rating_rating);
    END LOOP;
    CLOSE rating_cursor_friend;
    -- Finally, remove the friendship record and its reciprocal record
    DELETE FROM friend WHERE user = p_user AND friend = p_friend;
    DELETE FROM friend WHERE user = p_friend AND friend = p_user;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_or_update_rating(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_rating ENUM('one', 'two', 'three', 'four', 'five')
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE friend_id INT UNSIGNED;
    DECLARE rating_value DECIMAL(3,2);
    DECLARE old_rating_value DECIMAL(3,2);
    DECLARE existing_rating ENUM('one', 'two', 'three', 'four', 'five');
    DECLARE friend_cursor CURSOR FOR SELECT friend FROM friend WHERE user = p_user AND status = 'accepted';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    SET rating_value = FIELD(p_rating, 'one', 'two', 'three', 'four', 'five');
    SELECT rating INTO existing_rating FROM rating WHERE user = p_user AND post = p_post;
    SET done = false;

    IF existing_rating IS NOT NULL THEN
        SET old_rating_value = FIELD(existing_rating, 'one', 'two', 'three', 'four', 'five');
        OPEN friend_cursor;
        read_loop: LOOP
        FETCH friend_cursor INTO friend_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL update_rating_in_aggregate(friend_id, p_post, rating_value, old_rating_value);
        END LOOP;
        CLOSE friend_cursor;
        CALL update_rating_in_aggregate(p_user, p_post, rating_value, old_rating_value);
        UPDATE rating SET rating = p_rating, updated = CURRENT_TIMESTAMP WHERE user = p_user AND post = p_post;
    ELSE
        OPEN friend_cursor;
        read_loop: LOOP
            FETCH friend_cursor INTO friend_id;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL add_rating_to_aggregate(friend_id, p_post, rating_value);
        END LOOP;
        CLOSE friend_cursor;
        CALL add_rating_to_aggregate(p_user, p_post, rating_value);
        INSERT INTO rating (user, post, rating) VALUES (p_user, p_post, p_rating);
    END IF;

END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE delete_rating(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE friend_id INT UNSIGNED;
    DECLARE old_rating DECIMAL(3, 2);
    DECLARE friend_cursor CURSOR FOR
        SELECT friend FROM friend WHERE user = p_user AND status = 'accepted';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    SELECT FIELD(rating, 'one', 'two', 'three', 'four', 'five') INTO old_rating FROM rating WHERE user = p_user AND post = p_post;
    DELETE FROM rating WHERE user = p_user AND post = p_post;
    SET done = false;
    OPEN friend_cursor;
    read_loop: LOOP
        FETCH friend_cursor INTO friend_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL remove_rating_from_aggregate(friend_id, p_post, old_rating);
    END LOOP;
    CLOSE friend_cursor;
    CALL remove_rating_from_aggregate(p_user, p_post, old_rating);
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE delete_user(
    IN p_user_id INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE post_id INT UNSIGNED;
    DECLARE rating_cursor CURSOR FOR SELECT post FROM rating WHERE user = p_user_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    OPEN rating_cursor;
    rating_loop: LOOP
        FETCH rating_cursor INTO post_id;
        IF done THEN
            LEAVE rating_loop;
        END IF;

        CALL delete_rating(p_user_id, post_id);
    END LOOP;
    CLOSE rating_cursor;
    DELETE FROM user WHERE id = p_user_id;
END;
//
DELIMITER ;
