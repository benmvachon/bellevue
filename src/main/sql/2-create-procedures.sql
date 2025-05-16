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
    WHERE starter = true
    ORDER BY RAND()
    LIMIT 1;

    INSERT INTO equipment (user, item, equipped)
    VALUES (p_user_id, v_hat_id, true);

    COMMIT;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE calculate_popularity(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED,
    IN p_post INT UNSIGNED,
    OUT o_popularity INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE child INT UNSIGNED;
    DECLARE popularity INT UNSIGNED;
    DECLARE child_popularity INT UNSIGNED;
    DECLARE is_friend BOOLEAN DEFAULT FALSE;
    
    DECLARE post_cursor CURSOR FOR
        SELECT DISTINCT(p.id)
        FROM post p
        LEFT JOIN friend f ON p.user = f.friend AND f.user = p_user
        WHERE p.parent = p_post AND (f.status = 'ACCEPTED' OR p.user = p_user);

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SELECT EXISTS (
        SELECT 1 FROM post p
        LEFT JOIN friend f ON f.user = p_user AND f.friend = p.user
        WHERE p.id = p_post AND (p.user = p_user OR f.status = 'ACCEPTED')
    ) INTO is_friend;

    IF is_friend THEN
        SELECT a.popularity INTO popularity
        FROM aggregate_rating a
        WHERE a.user = p_user AND a.post = p_post;

        IF popularity IS NULL THEN
            SELECT COUNT(DISTINCT(r.user)) INTO popularity
            FROM rating r
            LEFT JOIN friend f ON f.user = p_user AND f.friend = r.user
            WHERE r.post = p_post
            AND (r.user = p_user OR f.status = 'ACCEPTED')
            AND (r.user != p_friend);

            SET done = FALSE;
            OPEN post_cursor;
            read_loop: LOOP
                FETCH post_cursor INTO child;
                IF done THEN
                    LEAVE read_loop;
                END IF;

                IF child IS NOT NULL THEN
                    SET child_popularity = 0;
                    CALL calculate_popularity(p_user, p_friend, child, child_popularity);
                    SET popularity = popularity + child_popularity + 1;
                END IF;
            END LOOP;
            CLOSE post_cursor;
        END IF;

        SET o_popularity = popularity;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_popularity_to_parent(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_popularity INT UNSIGNED
)
BEGIN
    DECLARE v_current_post INT UNSIGNED;
    DECLARE v_parent INT UNSIGNED;
    DECLARE v_author INT UNSIGNED;

    DECLARE all_visible BOOLEAN DEFAULT TRUE;

    DECLARE done INT DEFAULT FALSE;
    DECLARE ancestor INT UNSIGNED;
    DECLARE ancestor_cursor CURSOR FOR SELECT a.ancestor FROM temp_visible_ancestors a;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Use a temporary table to store all ancestor post IDs
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_visible_ancestors (
        ancestor INT UNSIGNED PRIMARY KEY
    ) ENGINE = MEMORY;

    SET v_current_post = p_post;

    visibility_check_loop: LOOP
        -- Get the parent of the current post
        SELECT parent INTO v_parent FROM post WHERE id = v_current_post;

        -- If no more parents, break the loop
        IF v_parent IS NULL THEN
            LEAVE visibility_check_loop;
        END IF;

        -- Get the author of this parent post
        SELECT user INTO v_author FROM post WHERE id = v_parent;

        -- Check visibility: either it's the user themselves, or they're friends
        IF v_author != p_user THEN
            IF NOT EXISTS (
                SELECT 1 FROM friend
                WHERE user = p_user AND friend = v_author AND status = 'ACCEPTED'
            ) THEN
                SET all_visible = FALSE;
                LEAVE visibility_check_loop;
            END IF;
        END IF;

        -- Add this parent to the list of visible ancestors
        INSERT IGNORE INTO temp_visible_ancestors (ancestor) VALUES (v_parent);

        -- Move up the chain
        SET v_current_post = v_parent;
    END LOOP;

    -- If visibility check passed for all ancestors, update each one
    IF all_visible THEN
        OPEN ancestor_cursor;

        ancestor_loop: LOOP
            FETCH ancestor_cursor INTO ancestor;
            IF done THEN
                LEAVE ancestor_loop;
            END IF;

            INSERT INTO aggregate_rating (user, post, rating, rating_count, popularity)
            VALUES (p_user, ancestor, 0, 0, p_popularity)
            ON DUPLICATE KEY UPDATE popularity = popularity + VALUES(popularity);
        END LOOP;

        CLOSE ancestor_cursor;
    END IF;

    DROP TEMPORARY TABLE IF EXISTS temp_visible_ancestors;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE remove_popularity_from_parent(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_popularity INT UNSIGNED
)
BEGIN
    DECLARE parent INT UNSIGNED;
    SELECT p.parent INTO parent FROM post p WHERE p.id = p_post;

    IF parent IS NOT NULL THEN
        UPDATE aggregate_rating a SET popularity = a.popularity - p_popularity WHERE a.user = p_user AND a.post = parent;
        CALL remove_popularity_from_parent(p_user, parent, p_popularity);
    END IF;
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
    DECLARE v_post INT UNSIGNED DEFAULT p_post;
    DECLARE v_author INT UNSIGNED;
    DECLARE v_parent INT UNSIGNED;
    DECLARE is_friend BOOLEAN DEFAULT TRUE;

    ancestor_check: LOOP
        -- Get the author and parent of the current post
        SELECT user, parent INTO v_author, v_parent FROM post WHERE id = v_post;

        -- If the user is not the same as p_user, check friendship
        IF v_author != p_user THEN
            IF NOT EXISTS (
                SELECT 1 FROM friend
                WHERE user = p_user AND friend = v_author AND status = 'ACCEPTED'
            ) THEN
                SET is_friend = FALSE;
                LEAVE ancestor_check;
            END IF;
        END IF;

        -- Move to the parent post
        IF v_parent IS NULL THEN
            LEAVE ancestor_check;
        ELSE
            SET v_post = v_parent;
        END IF;
    END LOOP;

    -- If all ancestor authors are friends, proceed with update
    IF is_friend THEN
        IF NOT EXISTS (
            SELECT 1 FROM aggregate_rating
            WHERE user = p_user AND post = p_post
        ) THEN
            INSERT INTO aggregate_rating (user, post, rating, rating_count, popularity)
            VALUES (p_user, p_post, p_rating, 1, 1);
        ELSE
            UPDATE aggregate_rating a
            SET rating = (a.rating * a.rating_count + p_rating) / (a.rating_count + 1),
                rating_count = a.rating_count + 1,
                popularity = a.popularity + 1,
                updated = CURRENT_TIMESTAMP
            WHERE user = p_user AND post = p_post;
        END IF;
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
    UPDATE aggregate_rating a
    SET rating = (a.rating * a.rating_count - p_old_rating + p_new_rating) / a.rating_count,
    updated = CURRENT_TIMESTAMP
    WHERE user = p_user AND post = p_post;
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
            UPDATE aggregate_rating a
            SET rating = stored_rating,
            rating_count = stored_count,
            popularity = a.popularity - 1,
            updated = CURRENT_TIMESTAMP
            WHERE user = p_user AND post = p_post;
        END IF;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE accept_child_posts(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED,
    IN p_post INT UNSIGNED
)
BEGIN
    DECLARE child INT UNSIGNED;
    DECLARE rating_count INT UNSIGNED;
    DECLARE rating DECIMAL(3,2);
    DECLARE done INT DEFAULT FALSE;

    DECLARE children CURSOR FOR
        SELECT p.id FROM post p
        LEFT JOIN friend f ON f.user = p_friend AND f.friend = p.user
        WHERE p.parent = p_post
        AND (f.status = 'ACCEPTED' OR p.user = p_friend)
        AND p.user != p_user;

    -- process children of p_post
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SET done = FALSE;
    OPEN children;
    read_loop: LOOP
        FETCH children INTO child;
        IF done THEN
            LEAVE read_loop;
        END IF;
        IF child IS NOT NULL THEN
            SELECT COUNT(DISTINCT(r.user)), AVG(r.rating) INTO rating_count, rating
            FROM rating r LEFT JOIN friend f ON f.user = p_friend AND f.friend = r.user
            WHERE r.post = child AND (r.user = p_friend OR f.status = 'ACCEPTED') AND (r.user != p_user);

            INSERT INTO aggregate_rating(post, user, rating, rating_count, popularity)
            VALUES (child, p_friend, rating, rating_count, rating_count);

            CALL add_popularity_to_parent(p_friend, child, rating_count + 1);
            CALL accept_child_posts(p_user, p_friend, child);
            SET done = FALSE;
        END IF;
    END LOOP;
    CLOSE children;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE accept_posts(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN
    DECLARE post INT UNSIGNED;
    DECLARE rating_count INT UNSIGNED;
    DECLARE rating DECIMAL(3,2);
    DECLARE done INT DEFAULT FALSE;

    DECLARE posts CURSOR FOR SELECT id FROM post WHERE user = p_user;

    -- Process posts by p_user
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SET done = FALSE;
    OPEN posts;
    read_loop: LOOP
        FETCH posts INTO post;
        IF done THEN
            LEAVE read_loop;
        END IF;
        IF post IS NOT NULL THEN

            SELECT COUNT(DISTINCT(r.user)), AVG(r.rating) INTO rating_count, rating
            FROM rating r LEFT JOIN friend f ON f.user = p_friend AND f.friend = r.user
            WHERE r.post = post AND (r.user = p_friend OR f.status = 'ACCEPTED') AND (r.user != p_user);

            INSERT INTO aggregate_rating(post, user, rating, rating_count, popularity)
            VALUES (post, p_friend, rating, rating_count, rating_count);

            CALL add_popularity_to_parent(p_friend, post, rating_count + 1);
            CALL accept_child_posts(p_user, p_friend, post);
            SET done = FALSE;
        END IF;
    END LOOP;
    CLOSE posts;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE accept_ratings(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN
    DECLARE post INT UNSIGNED;
    DECLARE rating DECIMAL(3,2);
    DECLARE done INT DEFAULT FALSE;

    DECLARE ratings CURSOR FOR
    SELECT r.post, FIELD(r.rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE') AS rating
    FROM rating r
    JOIN post p ON p.id = r.post
    LEFT JOIN friend f ON f.user = p_friend AND f.friend = p.user AND f.status = 'ACCEPTED'
    WHERE r.user = p_user
    AND (p.user = p_friend OR f.user IS NOT NULL);

    -- Process ratings by p_user
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SET done = FALSE;
    OPEN ratings;
    read_loop: LOOP
        FETCH ratings INTO post, rating;
        IF done THEN
            LEAVE read_loop;
        END IF;
        IF post IS NOT NULL THEN
            IF rating IS NOT NULL THEN
                CALL add_rating_to_aggregate(p_friend, post, rating);
                CALL add_popularity_to_parent(p_friend, post, 1);
                SET done = FALSE;
            END IF;
        END IF;
    END LOOP;
    CLOSE ratings;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE accept_friend(
    IN p_user INT UNSIGNED,
    IN p_friend INT UNSIGNED
)
BEGIN

    -- Check if the friendship already exists with pending status
    IF EXISTS (
        SELECT 1 FROM friend WHERE user = p_user AND friend = p_friend AND status = 'PENDING_YOU'
    ) THEN
        -- Update friendship statuses
        INSERT INTO friend (user, friend, status)
        VALUES (p_user, p_friend, 'ACCEPTED')
        ON DUPLICATE KEY UPDATE status = 'ACCEPTED', updated = CURRENT_TIMESTAMP;

        INSERT INTO friend (user, friend, status)
        VALUES (p_friend, p_user, 'ACCEPTED')
        ON DUPLICATE KEY UPDATE status = 'ACCEPTED', updated = CURRENT_TIMESTAMP;

        -- Iterate through the newly visible records for each user
        CALL accept_posts(p_user, p_friend);
        CALL accept_posts(p_friend, p_user);
        CALL accept_ratings(p_user, p_friend);
        CALL accept_ratings(p_friend, p_user);
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
    -- Insert the friendship record with status 'PENDING_THEM' to facilitate acceptance
    INSERT INTO friend (user, friend, status)
    VALUES (p_user, p_friend, 'PENDING_THEM')
    ON DUPLICATE KEY UPDATE status = 'PENDING_THEM', updated = CURRENT_TIMESTAMP;
    -- Insert the reciprocal friendship record with status 'PENDING_YOU' to facilitate acceptance
    INSERT INTO friend (user, friend, status)
    VALUES (p_friend, p_user, 'PENDING_YOU')
    ON DUPLICATE KEY UPDATE status = 'PENDING_YOU', updated = CURRENT_TIMESTAMP;
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
    DECLARE post INT UNSIGNED;
    DECLARE popularity INT UNSIGNED;
    DECLARE done INT DEFAULT FALSE;
    -- Declare the cursors and handlers at the start
    DECLARE friend_rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE') AS rating FROM rating WHERE user = p_friend;
    DECLARE user_rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE') AS rating FROM rating WHERE user = p_user;
    DECLARE post_cursor CURSOR FOR SELECT id FROM post WHERE user = p_user;
    DECLARE post_cursor_friend CURSOR FOR SELECT id FROM post WHERE user = p_friend;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Check if the friendship already exists
    IF EXISTS (
        SELECT * FROM friend
        WHERE user = p_user AND friend = p_friend
    ) THEN
        -- Update the status to 'blocked'
        UPDATE friend 
        SET status = 'BLOCKED_THEM', updated = CURRENT_TIMESTAMP 
        WHERE user = p_user AND friend = p_friend;
        -- Process ratings usered by the friend
        OPEN friend_rating_cursor;
        read_loop: LOOP
            FETCH friend_rating_cursor INTO rating_post, rating_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL remove_rating_from_aggregate(p_user, rating_post, rating_rating);
            CALL remove_popularity_from_parent(p_user, rating_post);
        END LOOP;
        CLOSE friend_rating_cursor;
        SET done = FALSE;
        -- Process posts authored by p_user
        OPEN post_cursor;
        read_loop: LOOP
            FETCH post_cursor INTO post;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL calculate_popularity(p_friend, p_user, post, popularity);
            IF popularity IS NOT NULL THEN
                DELETE FROM aggregate_rating WHERE a.post = post AND a.user = p_friend;
                CALL remove_popularity_from_parent(p_friend, post, popularity);
            END IF;
        END LOOP;
        CLOSE post_cursor;
        SET done = FALSE;
        -- Process ratings usered by the user
        OPEN user_rating_cursor;
        read_loop: LOOP
            FETCH user_rating_cursor INTO rating_post, rating_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL remove_rating_from_aggregate(p_friend, rating_post, rating_rating);
            CALL remove_popularity_from_parent(p_friend, rating_post);
        END LOOP;
        SET done = FALSE;
        -- Process posts authored by p_user
        OPEN post_cursor_friend;
        read_loop: LOOP
            FETCH post_cursor_friend INTO post;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL calculate_popularity(p_user, p_friend, post, popularity);
            IF popularity IS NOT NULL THEN
                DELETE FROM aggregate_rating WHERE a.post = post AND a.user = p_user;
                CALL remove_popularity_from_parent(p_user, post, popularity);
            END IF;
        END LOOP;
        CLOSE post_cursor_friend;
        SET done = FALSE;
        CLOSE user_rating_cursor;
    ELSE
        -- If the friendship does not exist, just insert a blocked entry
        INSERT INTO friend (user, friend, status)
        VALUES (p_user, p_friend, 'BLOCKED_THEM');
    END IF;
    -- Insert the reciprocal friendship record with status 'BLOCKED_YOU' to prevent visibility
    INSERT INTO friend (user, friend, status)
    VALUES (p_friend, p_user, 'BLOCKED_YOU')
    ON DUPLICATE KEY UPDATE status = 'BLOCKED_YOU', updated = CURRENT_TIMESTAMP;
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
    DECLARE post INT UNSIGNED;
    DECLARE popularity INT UNSIGNED;
    -- Cursor for ratings usered by p_user
    DECLARE rating_cursor CURSOR FOR 
        SELECT post, FIELD(rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE') AS rating FROM rating WHERE user = p_user;
    -- Cursor for ratings usered by p_friend
    DECLARE rating_cursor_friend CURSOR FOR 
        SELECT post, FIELD(rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE') AS rating FROM rating WHERE user = p_friend;
    DECLARE post_cursor CURSOR FOR SELECT id FROM post WHERE user = p_user;
    DECLARE post_cursor_friend CURSOR FOR SELECT id FROM post WHERE user = p_friend;
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
        CALL remove_popularity_from_parent(p_user, rating_post);
    END LOOP;
    CLOSE rating_cursor;
    -- Reset done flag for the second cursor
    SET done = FALSE;
    -- Process posts authored by p_user
    OPEN post_cursor;
    read_loop: LOOP
        FETCH post_cursor INTO post;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL calculate_popularity(p_friend, p_user, post, popularity);
        IF popularity IS NOT NULL THEN
            UPDATE aggregate_rating a SET a.popularity = popularity WHERE a.post = post AND a.user = p_friend;
            CALL remove_popularity_from_parent(p_friend, post, popularity);
        END IF;
    END LOOP;
    CLOSE post_cursor;
    SET done = FALSE;
    -- Process ratings authored by p_friend
    -- Process ratings usered by p_friend
    OPEN rating_cursor_friend;
    read_loop_friend: LOOP
        FETCH rating_cursor_friend INTO rating_post, rating_rating;
        IF done THEN
            LEAVE read_loop_friend;
        END IF;
        CALL remove_rating_from_aggregate(p_user, rating_post, rating_rating);
        CALL remove_popularity_from_parent(p_user, rating_post);
    END LOOP;
    CLOSE rating_cursor_friend;
    -- Finally, remove the friendship record and its reciprocal record
    DELETE FROM friend WHERE user = p_user AND friend = p_friend;
    DELETE FROM friend WHERE user = p_friend AND friend = p_user;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_reply(
    IN p_user INT UNSIGNED,
    IN p_parent INT UNSIGNED,
    IN p_forum INT UNSIGNED,
    IN p_content TEXT,
    OUT o_post INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE friend_id INT UNSIGNED;
    DECLARE friend_cursor CURSOR FOR SELECT friend FROM friend WHERE user = p_user AND status = 'ACCEPTED';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    INSERT INTO post (user, parent, forum, content) VALUES (p_user, p_parent, p_forum, p_content);
    SET o_post = LAST_INSERT_ID();
    call add_popularity_to_parent(p_user, o_post, 1);
    OPEN friend_cursor;
    read_loop: LOOP
        FETCH friend_cursor INTO friend_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        call add_popularity_to_parent(friend_id, o_post, 1);
    END LOOP;
    CLOSE friend_cursor;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_or_update_rating(
    IN p_user INT UNSIGNED,
    IN p_post INT UNSIGNED,
    IN p_rating ENUM('ONE', 'TWO', 'THREE', 'FOUR', 'FIVE')
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE friend_id INT UNSIGNED;
    DECLARE rating_value DECIMAL(3,2);
    DECLARE old_rating_value DECIMAL(3,2);
    DECLARE existing_rating ENUM('ONE', 'TWO', 'THREE', 'FOUR', 'FIVE');
    DECLARE friend_cursor CURSOR FOR SELECT friend FROM friend WHERE user = p_user AND status = 'ACCEPTED';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    SET rating_value = FIELD(p_rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE');
    SELECT rating INTO existing_rating FROM rating WHERE user = p_user AND post = p_post;
    SET done = false;

    IF existing_rating IS NOT NULL THEN
        SET old_rating_value = FIELD(existing_rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE');
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
            CALL add_popularity_to_parent(friend_id, p_post, 1);
        END LOOP;
        CLOSE friend_cursor;
        CALL add_rating_to_aggregate(p_user, p_post, rating_value);
        CALL add_popularity_to_parent(p_user, p_post, 1);
        INSERT INTO rating (user, post, rating) VALUES (p_user, p_post, p_rating);
    END IF;

END;
//
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
        SELECT friend FROM friend WHERE user = p_user AND status = 'ACCEPTED';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    SELECT FIELD(rating, 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE') INTO old_rating FROM rating WHERE user = p_user AND post = p_post;
    DELETE FROM rating WHERE user = p_user AND post = p_post;
    SET done = false;
    OPEN friend_cursor;
    read_loop: LOOP
        FETCH friend_cursor INTO friend_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL remove_rating_from_aggregate(friend_id, p_post, old_rating);
        CALL remove_popularity_from_parent(p_user, rating_post);
    END LOOP;
    CLOSE friend_cursor;
    CALL remove_rating_from_aggregate(p_user, p_post, old_rating);
    CALL remove_popularity_from_parent(p_user, rating_post);
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
