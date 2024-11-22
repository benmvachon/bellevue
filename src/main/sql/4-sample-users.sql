-- add sample user data
INSERT INTO user (name, username, password, avatar) VALUES
  ('Ben Vachon', 'bevis_armada', '$2a$10$/7zTkIlyp6YY04MGU0/LMOX4UI5svjyNSJ.mkMbAfSg6wEVntGsZa', 'cat'),
  ('Laura Zhang', 'biggie', '$2a$10$UneYqxe0x84eVAHDuA6MaO8/Sw6r1zZcgRFvQd.avJMZ1aTXOa42a', 'monkey'),
  ('Alice Johnson', 'alice_j', '$2a$10$SCJY/W1klZXC8swa2pl90uO9LptQb.w4p6dxIjzbLpScqJv0nSopy', 'walrus'),
  ('Michael Smith', 'mike_smith', '$2a$10$gAZzSVjf7xvsDrz1qE..u.Fa7YUhF5hKKk5IT7f2w7EvGKvlAupVi', 'horse'),
  ('Emma Brown', 'emmy_b', '$2a$10$/BenNoWChedoN2a8gn/QSu4J5uWW.SqiusGCFPXyof6mWWO1xZCPK', 'raptor'),
  ('Olivia Williams', 'liv_w', '$2a$10$vdVo54VT.LexIRDNLSVR9eAe67FTry2TN7u1tnwhdjY57bWw3159u', 'bee'),
  ('Noah Davis', 'noah_d', '$2a$10$jLJfunQuFO7wrdx.gbipR.cyuM.FmZRLUmsSO8j2af5V7FJW4kEWG', 'cat'),
  ('Ava Miller', 'ava_m', '$2a$10$cw8WT0tthFWaPQ2eNZlhXOaKOe1Hv1RZdBaZ78T6265Am8KfP8yW2', 'monkey'),
  ('Liam Wilson', 'liam_wil', '$2a$10$CO9FbGvcOJQOdWyBVz7yy.aBBglgkgXhXlwOb71TEgScHqD8LnG/m', 'walrus'),
  ('Sophia Moore', 'sophia_moore', '$2a$10$i20WeHlHKNf1prdZpyPe..8msvcAs87g9M5uGckBU4Wh1TpA1NGnS', 'horse'),
  ('James Taylor', 'jamestay', '$2a$10$shepmL.OJxmrODAxloYX6.jKTHHy.2oIMgJ2yeDvipXtd4J6RAhcG', 'raptor'),
  ('Isabella Anderson', 'isa_anderson', '$2a$10$xIoGW.Rzrrd2Z2GXWK6md./C9QPovLevEoS4XSp0cMrR2X.Rmwc5O', 'cat'),
  ('Lucas Thomas', 'lucas_t', '$2a$10$mCFt/CKxOG5mOp9JXXuhrubJeM4vlv6Cs1K8x1QWjIEL.zMJPvMLy', 'monkey'),
  ('Mia Jackson', 'mia_jack', '$2a$10$CBehE9CASDbhOXzVxrUnJOPVR6SLAmxBllEO.HVbjjyo9wFIdnmOa', 'walrus'),
  ('Ethan White', 'ethan_w', '$2a$10$XMoyCHAqCbPv5ly12pyA8eUQPXFvhZiUHnATmFJgDxpZiGppwAla.', 'horse'),
  ('Charlotte Harris', 'charlie_h', '$2a$10$au/7gSwBL.AG/ZCush8nWeP7DhlClXSbpbEK1Ph3Z5m540y8XuMS6', 'raptor'),
  ('Aiden Martin', 'aiden_m', '$2a$10$SAhJwvcboD3ZubB8Eddyou3x4ZpG7MokBIMsMMZ40MU9N7uNgptm.', 'cat'),
  ('Harper Thompson', 'harper_t', '$2a$10$idT9xsFQhWBkppmn2ZlI8O.h39I503/7VcjvHhvA2UHdGYsNQ64Z6', 'monkey'),
  ('Jackson Garcia', 'jack_garcia', '$2a$10$qJJW33kjX242lCPW.bGMw.h22Wfq9ICm99v3wG/f33OQVy6Vm5f5W', 'walrus'),
  ('Amelia Martinez', 'amelia_mart', '$2a$10$1KXuVJX8XJCFn4szWQhCTe8/TVADvxIoXgl63.S3td6Crmmms6hc6', 'horse');

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
CALL request_friend(1, 3);
CALL accept_friend(3, 1);
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