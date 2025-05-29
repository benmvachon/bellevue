INSERT INTO avatar (name) VALUES ('cat'), ('raptor'), ('walrus'), ('bee'), ('monkey'), ('horse');
INSERT INTO item (name, slot, starter, unlockable) VALUES
('red_cap', 'hat', true, false),
('blue_cap', 'hat', true, false),
('yellow_cap', 'hat', true, false),
('green_cap', 'hat', true, false),
('black_hat', 'hat', true, false),
('white_hat', 'hat', true, false),
('brown_hat', 'hat', true, false);

INSERT INTO forum (name, description) VALUES
('Town Hall', 'Where neighbors gather to chat about anything and everything.'),
('News Station', 'Tuning in to the latest headlines, scoops, and spirited debates.'),
('Event Space', 'Booking the next big bash or planning a quiet little get-together.'),
('Airport', 'Swapping travel tales, itineraries, and packing secrets.'),
('Culinary Center', 'Cooking up recipes, restaurant tips, and midnight snack confessions.'),
('Book Store', 'Flipping through pages, sharing bookish thoughts, and trading reads.'),
('Museum', 'Admiring the past, decoding culture, and arguing about art.'),
('Movie Theater', 'Reviewing blockbusters, indie gems, and everything in between.'),
('Television Studio', 'Bingeing shows, dodging spoilers, and rating cliffhangers.'),
('Concert Hall', 'Tapping toes, singing along, and arguing over “best album ever.”'),
('Video Game Store', 'Leveling up, sharing strategies, and debating game lore.'),
('Board Game Store', 'Rolling dice, flipping cards, and settling rules disputes.'),
('Garden', 'Swapping seeds, showing off harvests, and chatting compost.'),
('Gym', 'Breaking a sweat, chasing gains, and dodging leg day.'),
('Liquor Store', 'Pouring pints, mixing drinks, and toasting to taste.'),
('Workshop', 'Hammering, gluing, and proudly showing off slightly crooked shelves.'),
('Garage', 'Fixing engines, comparing mods, and bonding over busted bolts.'),
('Laboratory', 'Mixing ideas, testing theories, and geeking out over discoveries.'),
('Hacker Den', 'Coding, debugging, and sharing the occasional cursed regex.'),
('Court House', 'Debating laws, decoding policies, and delivering hot takes on justice.'),
('Hospital', 'Sharing health tips, wellness wins, and “should I see a doctor?” moments.'),
('Police Station', 'Talking true crime, neighborhood safety, and mystery solving.'),
('Playground', 'Swapping parenting wins, woes, and creative snack ideas.'),
('School', 'Trading tips on homework, lunchbox lore, and class projects.'),
('University', 'Studying hard, procrastinating harder, and philosophizing at 2 a.m.'),
('Temple', 'Lighting candles, asking big questions, and finding quiet moments.'),
('Train Station', 'Catching trains, complaining about delays, and sharing commute hacks.'),
('Office', 'Vent sessions, work wins, and career advice from the watercooler crowd.'),
('Park', 'Stretching legs, tossing ideas around, and enjoying the open air of random chat.'),
('Recycling Center', 'Sorting thoughts on sustainability, zero waste, and greener living.'),
('Shelter', 'Offering support, sharing resources, and being there for one another.');

-- 1 Town Hall
INSERT INTO forum_tag (forum, tag) VALUES (1, 'community'), (1, 'chat'), (1, 'general');

-- 2 News Station
INSERT INTO forum_tag (forum, tag) VALUES (2, 'news'), (2, 'debate'), (2, 'current-events');

-- 3 Event Space
INSERT INTO forum_tag (forum, tag) VALUES (3, 'events'), (3, 'planning'), (3, 'social');

-- 4 Airport
INSERT INTO forum_tag (forum, tag) VALUES (4, 'travel'), (4, 'flights'), (4, 'tips');

-- 5 Culinary Center
INSERT INTO forum_tag (forum, tag) VALUES (5, 'food'), (5, 'recipes'), (5, 'restaurants');

-- 6 Book Store
INSERT INTO forum_tag (forum, tag) VALUES (6, 'books'), (6, 'reading'), (6, 'literature');

-- 7 Museum
INSERT INTO forum_tag (forum, tag) VALUES (7, 'culture'), (7, 'history'), (7, 'art');

-- 8 Movie Theater
INSERT INTO forum_tag (forum, tag) VALUES (8, 'movies'), (8, 'cinema'), (8, 'reviews');

-- 9 Television Studio
INSERT INTO forum_tag (forum, tag) VALUES (9, 'tv'), (9, 'shows'), (9, 'series');

-- 10 Concert Hall
INSERT INTO forum_tag (forum, tag) VALUES (10, 'music'), (10, 'concerts'), (10, 'albums');

-- 11 Video Game Store
INSERT INTO forum_tag (forum, tag) VALUES (11, 'games'), (11, 'gaming'), (11, 'strategy');

-- 12 Board Game Store
INSERT INTO forum_tag (forum, tag) VALUES (12, 'board-games'), (12, 'tabletop'), (12, 'dice');

-- 13 Garden
INSERT INTO forum_tag (forum, tag) VALUES (13, 'gardening'), (13, 'plants'), (13, 'compost');

-- 14 Gym
INSERT INTO forum_tag (forum, tag) VALUES (14, 'fitness'), (14, 'workout'), (14, 'health');

-- 15 Liquor Store
INSERT INTO forum_tag (forum, tag) VALUES (15, 'drinks'), (15, 'alcohol'), (15, 'cocktails');

-- 16 Workshop
INSERT INTO forum_tag (forum, tag) VALUES (16, 'diy'), (16, 'tools'), (16, 'crafts');

-- 17 Garage
INSERT INTO forum_tag (forum, tag) VALUES (17, 'cars'), (17, 'repairs'), (17, 'mechanics');

-- 18 Laboratory
INSERT INTO forum_tag (forum, tag) VALUES (18, 'science'), (18, 'experiments'), (18, 'research');

-- 19 Hacker Den
INSERT INTO forum_tag (forum, tag) VALUES (19, 'coding'), (19, 'programming'), (19, 'tech');

-- 20 Court House
INSERT INTO forum_tag (forum, tag) VALUES (20, 'law'), (20, 'policy'), (20, 'debate');

-- 21 Hospital
INSERT INTO forum_tag (forum, tag) VALUES (21, 'health'), (21, 'wellness'), (21, 'medical');

-- 22 Police Station
INSERT INTO forum_tag (forum, tag) VALUES (22, 'crime'), (22, 'safety'), (22, 'mystery');

-- 23 Playground
INSERT INTO forum_tag (forum, tag) VALUES (23, 'parenting'), (23, 'kids'), (23, 'family');

-- 24 School
INSERT INTO forum_tag (forum, tag) VALUES (24, 'education'), (24, 'homework'), (24, 'students');

-- 25 University
INSERT INTO forum_tag (forum, tag) VALUES (25, 'college'), (25, 'academics'), (25, 'philosophy');

-- 26 Temple
INSERT INTO forum_tag (forum, tag) VALUES (26, 'spirituality'), (26, 'faith'), (26, 'reflection');

-- 27 Train Station
INSERT INTO forum_tag (forum, tag) VALUES (27, 'commute'), (27, 'trains'), (27, 'travel');

-- 28 Office
INSERT INTO forum_tag (forum, tag) VALUES (28, 'work'), (28, 'career'), (28, 'office-life');

-- 29 Park
INSERT INTO forum_tag (forum, tag) VALUES (29, 'outdoors'), (29, 'chat'), (29, 'relaxation');

-- 30 Recycling Center
INSERT INTO forum_tag (forum, tag) VALUES (30, 'sustainability'), (30, 'environment'), (30, 'green-living');

-- 31 Shelter
INSERT INTO forum_tag (forum, tag) VALUES (31, 'support'), (31, 'community'), (31, 'resources');
