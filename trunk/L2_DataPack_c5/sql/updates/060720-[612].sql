ALTER TABLE `characters` ADD `noble` decimal(2,0) default 0;
update characters set noble = 0;
ALTER TABLE `characters` ADD `varka` decimal(5,0) default 0;
update characters set varka = 0;
ALTER TABLE `characters` ADD `ketra` decimal(5,0) default 0;
update characters set ketra = 0;

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016785, "", 1, 5317, 86381, -75534, -3486, 0, 0, 20156, 60, 0, 0);

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016786, "", 1, 5317, 86902, -75649, -3496, 0, 0, 62502, 60, 0, 0);

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016787, "", 1, 5317, 86793, -75410, -3496, 0, 0, 20848, 60, 0, 0);

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016788, "", 1, 5317, 86404, -75276, -3480, 0, 0, 29007, 60, 0, 0);

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016789, "", 1, 5317, 86551, -75749, -3485, 0, 0, 47450, 60, 0, 0);

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016790, "", 1, 5317, 87007, -76074, -3485, 0, 0, 59762, 60, 0, 0);

INSERT INTO spawnlist
  (id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, periodOfDay)
VALUES
  (1249016791, "", 1, 5317, 86993, -75263, -3496, 0, 0, 11889, 60, 0, 0);