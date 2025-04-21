SET @p_output_id = 0; -- Initialize the variable
SET @p_output_avatar = ''; -- Initialize the variable
SET @p_output_hat = ''; -- Initialize the variable

-- add sample user data
CALL add_user('Ben Vachon', 'bevis_armada', 'bevis_armada@blaurvis.com', '$2a$10$/7zTkIlyp6YY04MGU0/LMOX4UI5svjyNSJ.mkMbAfSg6wEVntGsZa', @p_output_id, @p_output_avatar, @p_output_hat);        -- id 1
CALL add_user('Laura Zhang', 'biggie', 'biggie@blaurvis.com', '$2a$10$UneYqxe0x84eVAHDuA6MaO8/Sw6r1zZcgRFvQd.avJMZ1aTXOa42a', @p_output_id, @p_output_avatar, @p_output_hat);                   -- id 2
CALL add_user('Alice Johnson', 'alice_j', 'alice_j@blaurvis.com', '$2a$10$SCJY/W1klZXC8swa2pl90uO9LptQb.w4p6dxIjzbLpScqJv0nSopy', @p_output_id, @p_output_avatar, @p_output_hat);               -- id 3
CALL add_user('Michael Smith', 'mike_smith', 'mike_smith@blaurvis.com', '$2a$10$gAZzSVjf7xvsDrz1qE..u.Fa7YUhF5hKKk5IT7f2w7EvGKvlAupVi', @p_output_id, @p_output_avatar, @p_output_hat);         -- id 4
CALL add_user('Emma Brown', 'emmy_b', 'emmy_b@blaurvis.com', '$2a$10$/BenNoWChedoN2a8gn/QSu4J5uWW.SqiusGCFPXyof6mWWO1xZCPK', @p_output_id, @p_output_avatar, @p_output_hat);                    -- id 5
CALL add_user('Olivia Williams', 'liv_w', 'liv_w@blaurvis.com', '$2a$10$vdVo54VT.LexIRDNLSVR9eAe67FTry2TN7u1tnwhdjY57bWw3159u', @p_output_id, @p_output_avatar, @p_output_hat);                 -- id 6
CALL add_user('Noah Davis', 'noah_d', 'noah_d@blaurvis.com', '$2a$10$jLJfunQuFO7wrdx.gbipR.cyuM.FmZRLUmsSO8j2af5V7FJW4kEWG', @p_output_id, @p_output_avatar, @p_output_hat);                    -- id 7
CALL add_user('Ava Miller', 'ava_m', 'ava_m@blaurvis.com', '$2a$10$cw8WT0tthFWaPQ2eNZlhXOaKOe1Hv1RZdBaZ78T6265Am8KfP8yW2', @p_output_id, @p_output_avatar, @p_output_hat);                      -- id 8
CALL add_user('Liam Wilson', 'liam_wil', 'liam_wil@blaurvis.com', '$2a$10$CO9FbGvcOJQOdWyBVz7yy.aBBglgkgXhXlwOb71TEgScHqD8LnG/m', @p_output_id, @p_output_avatar, @p_output_hat);               -- id 9
CALL add_user('Sophia Moore', 'sophia_moore', 'sophia_moore@blaurvis.com', '$2a$10$i20WeHlHKNf1prdZpyPe..8msvcAs87g9M5uGckBU4Wh1TpA1NGnS', @p_output_id, @p_output_avatar, @p_output_hat);      -- id 10
CALL add_user('James Taylor', 'jamestay', 'jamestay@blaurvis.com', '$2a$10$shepmL.OJxmrODAxloYX6.jKTHHy.2oIMgJ2yeDvipXtd4J6RAhcG', @p_output_id, @p_output_avatar, @p_output_hat);              -- id 11
CALL add_user('Isabella Anderson', 'isa_anderson', 'isa_anderson@blaurvis.com', '$2a$10$xIoGW.Rzrrd2Z2GXWK6md./C9QPovLevEoS4XSp0cMrR2X.Rmwc5O', @p_output_id, @p_output_avatar, @p_output_hat); -- id 12
CALL add_user('Lucas Thomas', 'lucas_t', 'lucas_t@blaurvis.com', '$2a$10$mCFt/CKxOG5mOp9JXXuhrubJeM4vlv6Cs1K8x1QWjIEL.zMJPvMLy', @p_output_id, @p_output_avatar, @p_output_hat);                -- id 13
CALL add_user('Mia Jackson', 'mia_jack', 'mia_jack@blaurvis.com', '$2a$10$CBehE9CASDbhOXzVxrUnJOPVR6SLAmxBllEO.HVbjjyo9wFIdnmOa', @p_output_id, @p_output_avatar, @p_output_hat);               -- id 14
CALL add_user('Ethan White', 'ethan_w', 'ethan_w@blaurvis.com', '$2a$10$XMoyCHAqCbPv5ly12pyA8eUQPXFvhZiUHnATmFJgDxpZiGppwAla.', @p_output_id, @p_output_avatar, @p_output_hat);                 -- id 15
CALL add_user('Charlotte Harris', 'charlie_h', 'charlie_h@blaurvis.com', '$2a$10$au/7gSwBL.AG/ZCush8nWeP7DhlClXSbpbEK1Ph3Z5m540y8XuMS6', @p_output_id, @p_output_avatar, @p_output_hat);        -- id 16
CALL add_user('Aiden Martin', 'aiden_m', 'aiden_m@blaurvis.com', '$2a$10$SAhJwvcboD3ZubB8Eddyou3x4ZpG7MokBIMsMMZ40MU9N7uNgptm.', @p_output_id, @p_output_avatar, @p_output_hat);                -- id 17
CALL add_user('Harper Thompson', 'harper_t', 'harper_t@blaurvis.com', '$2a$10$idT9xsFQhWBkppmn2ZlI8O.h39I503/7VcjvHhvA2UHdGYsNQ64Z6', @p_output_id, @p_output_avatar, @p_output_hat);           -- id 18
CALL add_user('Jackson Garcia', 'jack_garcia', 'jack_garcia@blaurvis.com', '$2a$10$qJJW33kjX242lCPW.bGMw.h22Wfq9ICm99v3wG/f33OQVy6Vm5f5W', @p_output_id, @p_output_avatar, @p_output_hat);      -- id 19
CALL add_user('Amelia Martinez', 'amelia_mart', 'amelia_mart@blaurvis.com', '$2a$10$1KXuVJX8XJCFn4szWQhCTe8/TVADvxIoXgl63.S3td6Crmmms6hc6', @p_output_id, @p_output_avatar, @p_output_hat);     -- id 20

-- add sample user relationships
CALL request_friend(2, 1);
CALL accept_friend(1, 2);
CALL request_friend(3, 1);
CALL accept_friend(1, 3);
CALL request_friend(5, 1);
CALL accept_friend(1, 5);
CALL request_friend(6, 1);
CALL accept_friend(1, 6);
CALL request_friend(4, 2);
CALL accept_friend(2, 4);
CALL request_friend(5, 2);
CALL accept_friend(2, 5);
CALL request_friend(7, 2);
CALL accept_friend(2, 7);
CALL request_friend(4, 3);
CALL accept_friend(3, 4);
CALL request_friend(5, 3);
CALL accept_friend(3, 5);
CALL request_friend(6, 4);
CALL accept_friend(4, 6);
CALL request_friend(8, 4);
CALL accept_friend(4, 8);
CALL request_friend(7, 5);
CALL accept_friend(5, 7);
CALL request_friend(9, 5);
CALL accept_friend(5, 9);
CALL request_friend(10, 6);
CALL accept_friend(6, 10);
CALL request_friend(1, 7);
CALL accept_friend(7, 1);
CALL request_friend(2, 8);
CALL accept_friend(8, 2);
CALL request_friend(3, 9);
CALL accept_friend(9, 3);
CALL request_friend(4, 10);
CALL accept_friend(10, 4);
CALL request_friend(13, 10);
CALL accept_friend(10, 13);
CALL request_friend(12, 11);
CALL accept_friend(11, 12);
CALL request_friend(8, 11);
CALL accept_friend(11, 8);
CALL request_friend(13, 12);
CALL accept_friend(12, 13);
CALL request_friend(7, 12);
CALL accept_friend(12, 7);
CALL request_friend(14, 13);
CALL accept_friend(13, 14);
CALL request_friend(9, 13);
CALL accept_friend(13, 9);
CALL request_friend(15, 14);
CALL accept_friend(14, 15);
CALL request_friend(4, 14);
CALL accept_friend(14, 4);
CALL request_friend(16, 15);
CALL accept_friend(15, 16);
CALL request_friend(2, 15);
CALL accept_friend(15, 2);
CALL request_friend(17, 16);
CALL accept_friend(16, 17);
CALL request_friend(12, 16);
CALL accept_friend(16, 12);
CALL request_friend(18, 17);
CALL accept_friend(17, 18);
CALL request_friend(15, 17);
CALL accept_friend(17, 15);
CALL request_friend(19, 18);
CALL accept_friend(18, 19);
CALL request_friend(5, 18);
CALL accept_friend(18, 5);
CALL request_friend(20, 19);
CALL accept_friend(19, 20);
CALL request_friend(14, 19);
CALL accept_friend(19, 14);
CALL request_friend(1, 20);
CALL accept_friend(20, 1);
CALL request_friend(6, 20);
CALL accept_friend(20, 6);

CALL request_friend(1, 4);
CALL request_friend(2, 8);
CALL request_friend(3, 9);
CALL request_friend(5, 10);
CALL request_friend(6, 11);
CALL request_friend(7, 12);
CALL request_friend(8, 14);
CALL request_friend(9, 15);
CALL request_friend(10, 16);
CALL request_friend(11, 17);
CALL request_friend(12, 18);

CALL block_friend(1, 9);
CALL block_friend(2, 10);
CALL block_friend(3, 11);
CALL block_friend(4, 12);
CALL block_friend(5, 13);