INSERT INTO avatar (name) VALUES ('cat'), ('raptor'), ('walrus'), ('bee'), ('monkey'), ('horse');
INSERT INTO item (name, slot, starter, unlockable) VALUES
('red_cap', 'hat', true, false),
('blue_cap', 'hat', true, false),
('yellow_cap', 'hat', true, false),
('green_cap', 'hat', true, false),
('black_hat', 'hat', true, false),
('white_hat', 'hat', true, false),
('brown_hat', 'hat', true, false);

INSERT INTO forum (name, category) VALUES
('Announcements', 'General'),                           -- id 1
('Introductions & Welcome', 'General'),                 -- id 2
('General Discussion', 'General'),                      -- id 3
('Feedback & Suggestions', 'General'),                  -- id 4

('Software Development', 'Technology'),                 -- id 5

('Video Games', 'Gaming'),                              -- id 6
('Board Games', 'Gaming'),                              -- id 7
('Esports & Competitive Gaming', 'Gaming'),             -- id 8

('Movies', 'Entertainment'),                            -- id 9
('Television', 'Entertainment'),                        -- id 10
('Music', 'Entertainment'),                             -- id 11
('Podcasts', 'Entertainment'),                          -- id 12
('Literature', 'Entertainment'),                        -- id 13

('Travel', 'Lifestyle'),                                -- id 14
('Food & Cooking', 'Lifestyle'),                        -- id 15
('Restaurants & Bars', 'Lifestyle'),                    -- id 16
('Fitness & Health', 'Lifestyle'),                      -- id 17
('DIY & Home Improvement', 'Lifestyle'),                -- id 18

('Off-Topic Chat', 'Community & Social'),               -- id 19
('Local & Regional Discussions', 'Community & Social'), -- id 20
('Events & Meetups', 'Community & Social');             -- id 21

INSERT INTO notification_type (name) VALUES
('forum'),      -- id 1
('post'),       -- id 2
('reply'),      -- id 3
('rating'),     -- id 4
('request'),    -- id 5
('acceptance'), -- id 6
('message');    -- id 7