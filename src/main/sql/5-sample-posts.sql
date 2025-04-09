-- Original posts
INSERT INTO post (user, forum, content) VALUES
  (1, 1, 'Hey everyone!'),
  (3, 2, 'Everyone welcome Michael Smith!'),
  (5, 5, 'Anyone excited about the new Spring release?'),
  (6, 9, 'Just watched Dune Part 2—what a ride!'),
  (8, 15, 'I just made the best lasagna last night.'),
  (9, 6, 'Any favorite indie games you recommend?'),
  (12, 17, 'Trying to build a home workout routine.'),
  (14, 13, 'Currently obsessed with Brandon Sanderson.'),
  (15, 20, 'Anyone here from the Seattle area?'),
  (19, 21, 'We’re planning a community BBQ next month!');

-- Replies to existing posts
INSERT INTO post (user, parent, forum, content) VALUES
  (2, 1, 1, 'hey'),
  (7, 11, 1, 'will you two get a room?'),
  (4, 2, 2, 'Thanks, welcome to be here!'),
  (7, 3, 5, 'Yes! Spring Boot 3.3 is looking slick.'),
  (10, 4, 9, 'Dune was mind-blowing, totally agree.'),
  (11, 5, 15, 'Got a recipe to share?'),
  (13, 6, 6, 'Try Hollow Knight if you haven’t already!'),
  (16, 7, 17, 'Start simple: push-ups, planks, squats.'),
  (19, 8, 13, 'Mistborn trilogy is amazing.'),
  (17, 9, 20, 'Seattle here! Fremont is beautiful in spring.'),
  (20, 10, 21, 'Count me in! Let me know what to bring.');
