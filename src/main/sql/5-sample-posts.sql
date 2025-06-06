-- Original posts
INSERT INTO post (user, forum, content) VALUES
  (1, 1, 'Hey everyone!'),
  (3, 1, 'Everyone welcome Michael Smith!'),
  (5, 19, 'Anyone excited about the new Spring release?'),
  (6, 8, 'Just watched Dune Part 2—what a ride!'),
  (8, 5, 'I just made the best lasagna last night.'),
  (9, 11, 'Any favorite indie games you recommend?'),
  (12, 14, 'Trying to build a home workout routine.'),
  (14, 6, 'Currently obsessed with Brandon Sanderson.'),
  (15, 4, 'Anyone here from the Seattle area?'),
  (19, 3, 'We’re planning a community BBQ next month!');
CALL add_or_update_rating(1, 1, 'five');
CALL add_or_update_rating(3, 2, 'five');
CALL add_or_update_rating(5, 3, 'five');
CALL add_or_update_rating(6, 4, 'five');
CALL add_or_update_rating(8, 5, 'five');
CALL add_or_update_rating(9, 6, 'five');
CALL add_or_update_rating(12, 7, 'five');
CALL add_or_update_rating(14, 8, 'five');
CALL add_or_update_rating(15, 9, 'five');
CALL add_or_update_rating(19, 10, 'five');

SET @p_output_post = 0;
-- Replies to existing posts
CALL add_reply(2, 1, 1, 'hey', @p_output_post);
CALL add_reply(7, 11, 1, 'will you two get a room?', @p_output_post);
CALL add_reply(4, 2, 1, 'Thanks, welcome to be here!', @p_output_post);
CALL add_reply(7, 3, 19, 'Yes! Spring Boot 3.3 is looking slick.', @p_output_post);
CALL add_reply(10, 4, 8, 'Dune was mind-blowing, totally agree.', @p_output_post);
CALL add_reply(11, 5, 5, 'Got a recipe to share?', @p_output_post);
CALL add_reply(13, 6, 11, 'Try Hollow Knight if you haven’t already!', @p_output_post);
CALL add_reply(16, 7, 14, 'Start simple: push-ups, planks, squats.', @p_output_post);
CALL add_reply(19, 8, 6, 'Mistborn trilogy is amazing.', @p_output_post);
CALL add_reply(17, 9, 4, 'Seattle here! Fremont is beautiful in spring.', @p_output_post);
CALL add_reply(20, 10, 3, 'Count me in! Let me know what to bring.', @p_output_post);
CALL add_or_update_rating(2, 11, 'five');
CALL add_or_update_rating(7, 12, 'five');
CALL add_or_update_rating(4, 13, 'five');
CALL add_or_update_rating(7, 14, 'five');
CALL add_or_update_rating(10, 15, 'five');
CALL add_or_update_rating(11, 16, 'five');
CALL add_or_update_rating(13, 17, 'five');
CALL add_or_update_rating(16, 18, 'five');
CALL add_or_update_rating(19, 19, 'five');
CALL add_or_update_rating(17, 20, 'five');
CALL add_or_update_rating(20, 21, 'five');