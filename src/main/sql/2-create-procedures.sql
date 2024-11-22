DELIMITER //
CREATE PROCEDURE add_review_to_aggregate(
    IN p_user INT UNSIGNED,
    IN p_recipe INT UNSIGNED,
    IN p_rating DECIMAL(3,2)
)
BEGIN
    DECLARE stored_rating DECIMAL(3,2);
    DECLARE stored_count INT UNSIGNED;
    -- Check if the aggregate already exists
    IF NOT EXISTS (
        SELECT * FROM aggregate_rating
        WHERE user = p_user AND recipe = p_recipe
    ) THEN
        -- Add it if it doesn't
        INSERT INTO aggregate_rating (user, recipe, rating, rating_count)
        VALUES (p_user, p_recipe, p_rating, 1);
    ELSE
        -- Update it if it does
        SELECT rating, rating_count INTO stored_rating, stored_count
        FROM aggregate_rating
        WHERE user = p_user AND recipe = p_recipe;
        SET stored_count = stored_count + 1;
        SET stored_rating = (stored_rating * (stored_count - 1) + p_rating) / stored_count;
        UPDATE aggregate_rating 
        SET rating = stored_rating, rating_count = stored_count, updated = CURRENT_TIMESTAMP
        WHERE user = p_user AND recipe = p_recipe;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE update_review_in_aggregate(
    IN p_user INT UNSIGNED,
    IN p_recipe INT UNSIGNED,
    IN p_new_rating DECIMAL(3,2),
    IN p_old_rating DECIMAL(3,2)
)
BEGIN
    DECLARE stored_rating DECIMAL(3,2);
    DECLARE stored_count INT UNSIGNED;
    -- Check if the aggregate already exists (it really should because this was triggered by an update)
    IF NOT EXISTS (
        SELECT * FROM aggregate_rating
        WHERE user = p_user AND recipe = p_recipe
    ) THEN
    -- Add it if it doesn't
        INSERT INTO aggregate_rating (user, recipe, rating, rating_count)
        VALUES (p_user, p_recipe, p_new_rating, 1);
    ELSE
    -- Update it if it does
        SELECT rating, rating_count INTO stored_rating, stored_count
        FROM aggregate_rating
        WHERE user = p_user AND recipe = p_recipe;
        SET stored_rating = (stored_rating * stored_count - p_old_rating + p_new_rating) / stored_count;
        UPDATE aggregate_rating 
        SET rating = stored_rating,updated = CURRENT_TIMESTAMP
        WHERE user = p_user AND recipe = p_recipe;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE remove_review_from_aggregate(
    IN p_user INT UNSIGNED,
    IN p_recipe INT UNSIGNED,
    IN p_rating DECIMAL(3,2)
)
BEGIN
    DECLARE stored_rating DECIMAL(3,2);
    DECLARE stored_count INT UNSIGNED;
    -- Check if the aggregate exists
    IF EXISTS (
        SELECT * FROM aggregate_rating
        WHERE user = p_user AND recipe = p_recipe
    ) THEN
        -- Retrieve existing rating and count
        SELECT rating, rating_count INTO stored_rating, stored_count
        FROM aggregate_rating
        WHERE user = p_user AND recipe = p_recipe;
        -- Check if this is the last review
        IF stored_count = 1 THEN
            -- If this is the only review, delete the aggregate entry
            DELETE FROM aggregate_rating
            WHERE user = p_user AND recipe = p_recipe;
        ELSE
            -- Calculate new average rating after deletion
            SET stored_rating = (stored_rating * stored_count - p_rating) / (stored_count - 1);
            SET stored_count = stored_count - 1;
            -- Update the aggregate rating with the new average
            UPDATE aggregate_rating 
            SET rating = stored_rating, rating_count = stored_count, updated = CURRENT_TIMESTAMP
            WHERE user = p_user AND recipe = p_recipe;
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
    DECLARE review_user INT UNSIGNED;
    DECLARE review_recipe INT UNSIGNED;
    DECLARE review_rating DECIMAL(3,2);
    -- Cursor for reviews authored by p_user
    DECLARE review_cursor CURSOR FOR 
        SELECT recipe, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') AS rating FROM review WHERE author = p_user;
    -- Cursor for reviews authored by p_friend
    DECLARE review_cursor_friend CURSOR FOR 
        SELECT recipe, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') AS rating FROM review WHERE author = p_friend;
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
        -- Process reviews authored by p_user
        OPEN review_cursor;
        read_loop: LOOP
            FETCH review_cursor INTO review_recipe, review_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL add_review_to_aggregate(p_friend, review_recipe, review_rating);
        END LOOP;
        CLOSE review_cursor;
        -- Reset done flag for the second cursor
        SET done = FALSE;
        -- Process reviews authored by p_friend
        OPEN review_cursor_friend;
        read_loop_friend: LOOP
            FETCH review_cursor_friend INTO review_recipe, review_rating;
            IF done THEN
                LEAVE read_loop_friend;
            END IF;
            CALL add_review_to_aggregate(p_user, review_recipe, review_rating);
        END LOOP;
        CLOSE review_cursor_friend;
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
    VALUES (p_friend, p_user, 'pending_them')
    ON DUPLICATE KEY UPDATE 
        status = 'pending_them', updated = CURRENT_TIMESTAMP;
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
    DECLARE review_recipe INT UNSIGNED;
    DECLARE review_rating DECIMAL(3,2);
    DECLARE done INT DEFAULT FALSE;
    -- Declare the cursors and handlers at the start
    DECLARE friend_review_cursor CURSOR FOR 
        SELECT recipe, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') AS rating FROM review WHERE author = p_friend;
    DECLARE user_review_cursor CURSOR FOR 
        SELECT recipe, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') AS rating FROM review WHERE author = p_user;
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
        -- Process reviews authored by the friend
        OPEN friend_review_cursor;
        read_loop: LOOP
            FETCH friend_review_cursor INTO review_recipe, review_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL remove_review_from_aggregate(p_user, review_recipe, review_rating);
        END LOOP;
        CLOSE friend_review_cursor;
        -- Process reviews authored by the user
        OPEN user_review_cursor;
        read_loop: LOOP
            FETCH user_review_cursor INTO review_recipe, review_rating;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL remove_review_from_aggregate(p_friend, review_recipe, review_rating);
        END LOOP;
        CLOSE user_review_cursor;
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
    -- Remove reviews from the aggregate for both users
    DECLARE done INT DEFAULT FALSE;
    DECLARE review_recipe INT UNSIGNED;
    DECLARE review_rating DECIMAL(3,2);
    -- Cursor for reviews authored by p_user
    DECLARE review_cursor CURSOR FOR 
        SELECT recipe, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') AS rating FROM review WHERE author = p_user;
    -- Cursor for reviews authored by p_friend
    DECLARE review_cursor_friend CURSOR FOR 
        SELECT recipe, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') AS rating FROM review WHERE author = p_friend;
    -- Handler to exit the loop
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Process reviews authored by p_user
    OPEN review_cursor;
    read_loop: LOOP
        FETCH review_cursor INTO review_recipe, review_rating;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL remove_review_from_aggregate(p_friend, review_recipe, review_rating);
    END LOOP;
    CLOSE review_cursor;
    -- Reset done flag for the second cursor
    SET done = FALSE;
    -- Process reviews authored by p_friend
    OPEN review_cursor_friend;
    read_loop_friend: LOOP
        FETCH review_cursor_friend INTO review_recipe, review_rating;
        IF done THEN
            LEAVE read_loop_friend;
        END IF;
        CALL remove_review_from_aggregate(p_user, review_recipe, review_rating);
    END LOOP;
    CLOSE review_cursor_friend;
    -- Finally, remove the friendship record and its reciprocal record
    DELETE FROM friend WHERE user = p_user AND friend = p_friend;
    DELETE FROM friend WHERE user = p_friend AND friend = p_user;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE add_review(
    IN p_author INT UNSIGNED,
    IN p_recipe INT UNSIGNED,
    IN p_review ENUM('disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly'),
    IN p_content VARCHAR(255),
    OUT p_review_id INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE friend_id INT UNSIGNED;
    DECLARE rating DECIMAL(3, 2);
    -- Cursor for friends of the author
    DECLARE friend_cursor CURSOR FOR SELECT friend FROM friend WHERE user = p_author AND status = 'accepted';
    -- Handler to exit the loop
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Add the review
    IF p_review IS NOT NULL THEN
        SET rating = FIELD(p_review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly');
    ELSE
        SET rating = NULL;
    END IF;

    INSERT INTO review (author, recipe, review, content)
    VALUES (p_author, p_recipe, p_review, p_content);
    -- Get the last inserted ID
    SET p_review_id = LAST_INSERT_ID();

    -- Only update aggregates if p_review is not NULL
    IF p_review IS NOT NULL THEN
        OPEN friend_cursor;
        read_loop: LOOP
            FETCH friend_cursor INTO friend_id;
            IF done THEN
                LEAVE read_loop;
            END IF;
            CALL add_review_to_aggregate(friend_id, p_recipe, rating);
        END LOOP;
        CLOSE friend_cursor;

        CALL add_review_to_aggregate(p_author, p_recipe, rating);
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE update_review(
    IN p_review_id INT UNSIGNED,
    IN p_new_review ENUM('disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly'), -- this cannot be null
    IN p_new_content VARCHAR(255)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE recipe_id INT UNSIGNED;
    DECLARE author_id INT UNSIGNED;
    DECLARE friend_id INT UNSIGNED;
    DECLARE new_rating DECIMAL(3, 2);
    DECLARE old_rating DECIMAL(3, 2);
    -- Cursor for friends of the author
    DECLARE friend_cursor CURSOR FOR SELECT friend FROM friend WHERE user = author_id AND status = 'accepted';
    -- Handler to exit the loop
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    IF p_new_review IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'p_new_review cannot be NULL';
    END IF;

    SET new_rating = FIELD(p_new_review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly');
    -- Get the author and old rating of the review
    SELECT recipe, author, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') 
    INTO recipe_id, author_id, old_rating 
    FROM review WHERE id = p_review_id;

    -- Update the review
    UPDATE review
    SET review = p_new_review, content = p_new_content, updated = CURRENT_TIMESTAMP
    WHERE id = p_review_id;

    -- Process each friend to update their aggregates
    OPEN friend_cursor;
    read_loop: LOOP
        FETCH friend_cursor INTO friend_id;
        IF done THEN
            LEAVE read_loop;
        END IF;

        IF old_rating IS NULL THEN
            CALL add_review_to_aggregate(friend_id, recipe_id, new_rating);
        ELSE
            CALL update_review_in_aggregate(friend_id, recipe_id, new_rating, old_rating);
        END IF;
    END LOOP;
    CLOSE friend_cursor;

    -- Update the aggregate for the author as well
    IF old_rating IS NULL THEN
        CALL add_review_to_aggregate(author_id, recipe_id, new_rating);
    ELSE
        CALL update_review_in_aggregate(author_id, recipe_id, new_rating, old_rating);
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE start_cooking(
    IN p_user INT UNSIGNED,
    IN p_recipe INT UNSIGNED,
    OUT p_review_id INT UNSIGNED
)
BEGIN
    UPDATE user
    SET status = 'cooking'
    WHERE id = p_user;

    CALL add_review(p_user, p_recipe, NULL, NULL, p_review_id);
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE delete_review(
    IN p_review_id INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE author_id INT UNSIGNED;
    DECLARE friend_id INT UNSIGNED;
    DECLARE old_rating DECIMAL(3, 2);
    -- Cursor for friends of the author
    DECLARE friend_cursor CURSOR FOR SELECT friend FROM friend WHERE user = author_id AND status = 'accepted';
    -- Handler to exit the loop
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    -- Get the author and old rating of the review
    SELECT author, FIELD(review, 'disastrously', 'muddlingly', 'admirably', 'splendidly', 'jubilantly') INTO author_id, old_rating FROM review WHERE id = p_review_id;
    -- Update the review
    DELETE FROM review WHERE id = p_review_id;
    -- Process each friend to update their aggregates
    OPEN friend_cursor;
    read_loop: LOOP
        FETCH friend_cursor INTO friend_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        CALL remove_review_from_aggregate(friend_id, p_review_id, old_rating);
    END LOOP;
    CLOSE friend_cursor;
    -- update the aggregate for the author as well
    CALL remove_review_from_aggregate(author_id, p_review_id, old_rating);
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE delete_user(
    IN p_user_id INT UNSIGNED
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE review_id INT UNSIGNED;
    -- Cursor to find all reviews by the user
    DECLARE review_cursor CURSOR FOR SELECT id FROM review WHERE author = p_user_id;
    -- Handler to exit the loop when no more reviews are found
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Open the cursor
    OPEN review_cursor;
    review_loop: LOOP
        -- Fetch the next review id
        FETCH review_cursor INTO review_id;
        -- Check if we have reached the end of the result set
        IF done THEN
            LEAVE review_loop;
        END IF;

        -- Call delete_review procedure on each review found
        CALL delete_review(review_id);
    END LOOP;

    -- Close the cursor
    CLOSE review_cursor;

    -- Optionally, delete the user record if necessary
    DELETE FROM user WHERE id = p_user_id;
END;
//
DELIMITER ;
