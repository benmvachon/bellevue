-- Original posts
INSERT INTO post (user, forum, content) VALUES
  (3, 1, 'Everyone welcome Michael Smith!'),
  (5, 19, 'Anyone excited about the new Spring release?'),
  (6, 8, 'Just watched Dune Part 2—what a ride!'),
  (8, 5, 'I just made the best lasagna last night.'),
  (9, 11, 'Any favorite indie games you recommend?'),
  (12, 14, 'Trying to build a home workout routine.'),
  (14, 6, 'Currently obsessed with Brandon Sanderson.'),
  (15, 4, 'Anyone here from the Seattle area?'),
  (19, 3, 'We’re planning a community BBQ next month!');
CALL add_or_update_rating(3, 2, 'five');
CALL add_or_update_rating(5, 3, 'five');
CALL add_or_update_rating(6, 4, 'five');
CALL add_or_update_rating(8, 5, 'five');
CALL add_or_update_rating(9, 6, 'five');
CALL add_or_update_rating(12, 7, 'five');
CALL add_or_update_rating(14, 8, 'five');
CALL add_or_update_rating(15, 9, 'five');

SET @p_output_post = 0;
-- Replies to existing posts
CALL add_reply(4, 1, 1, 'Thanks, welcome to be here!', @p_output_post);
CALL add_reply(7, 2, 19, 'Yes! Spring Boot 3.3 is looking slick.', @p_output_post);
CALL add_reply(10, 3, 8, 'Dune was mind-blowing, totally agree.', @p_output_post);
CALL add_reply(11, 4, 5, 'Got a recipe to share?', @p_output_post);
CALL add_reply(13, 5, 11, 'Try Hollow Knight if you haven’t already!', @p_output_post);
CALL add_reply(16, 6, 14, 'Start simple: push-ups, planks, squats.', @p_output_post);
CALL add_reply(19, 7, 6, 'Mistborn trilogy is amazing.', @p_output_post);
CALL add_reply(17, 8, 4, 'Seattle here! Fremont is beautiful in spring.', @p_output_post);
CALL add_reply(20, 9, 3, 'Count me in! Let me know what to bring.', @p_output_post);
CALL add_or_update_rating(4, 10, 'five');
CALL add_or_update_rating(7, 11, 'five');
CALL add_or_update_rating(10, 12, 'five');
CALL add_or_update_rating(11, 13, 'five');
CALL add_or_update_rating(13, 14, 'five');
CALL add_or_update_rating(16, 15, 'five');
CALL add_or_update_rating(19, 16, 'five');
CALL add_or_update_rating(17, 17, 'five');
CALL add_or_update_rating(20, 18, 'five');