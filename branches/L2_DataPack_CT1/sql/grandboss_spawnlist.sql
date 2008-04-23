-- ---------------------------
-- Table structure for `raidboss_spawnlist`
-- ---------------------------
DROP TABLE IF EXISTS `grandboss_spawnlist`;
CREATE TABLE `grandboss_spawnlist` (
  `boss_id` INT NOT NULL default 0,
  `loc_x` INT NOT NULL default 0,
  `loc_y` INT NOT NULL default 0,
  `loc_z` INT NOT NULL default 0,
  `heading` INT NOT NULL default 0,
  `respawn_min_delay` INT(11) NOT NULL default '86400',
  `respawn_max_delay` INT(11) NOT NULL default '129600',
  `respawn_time` BIGINT NOT NULL default 0,
  `currentHp` decimal(8,0) default NULL,
  `currentMp` decimal(8,0) default NULL,
  PRIMARY KEY (`boss_id`,`loc_x`,`loc_y`,`loc_z`)
) DEFAULT CHARSET=utf8;

-- This table stores spawn infos for all L2 bosses not currently managed by a lair instance.

INSERT INTO `grandboss_spawnlist` VALUES
(29001, -21610, 181594, -5734, 0,     86400, 129600, 0, 229898, 667), -- Ant Queen (40)
(29006, 17726,  108915, -6480, 0,     86400, 129600, 0, 162561, 575), -- Core (50)
(29014, 55024,  17368,  -5412, 10126, 86400, 129600, 0, 325124, 1660), -- Orfen (50)
(29022, 55312,  219168, -3223, 0,     86400, 129600, 0, 858518, 1975), -- Zaken (60)
(22215, 24767, -12441,  -2532, 15314, 86400, 129600, 0, 306406, 2339), -- Tyrannosaurus (80)
-- (22215, 28263, -17486,  -2539, 50052, 86400, 129600, 0, 306406, 2339), -- Tyrannosaurus (80) -- TODO: Multiple instances per ID
-- (22215, 18229, -17975,  -3219, 65140, 86400, 129600, 0, 306406, 2339), -- Tyrannosaurus (80)
(22216, 19897, -9087,   -2781, 2686,  86400, 129600, 0, 306406, 2339), -- Tyrannosaurus (80)
(22217, 22827, -14698,  -3080, 53946, 86400, 129600, 0, 306406, 2339); -- Tyrannosaurus (80)