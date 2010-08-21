CREATE TABLE IF NOT EXISTS `grandboss_spawnlist` (
  `boss_id` INT NOT NULL DEFAULT 0,
  `loc_x` INT NOT NULL DEFAULT 0,
  `loc_y` INT NOT NULL DEFAULT 0,
  `loc_z` INT NOT NULL DEFAULT 0,
  `heading` INT NOT NULL DEFAULT 0,
  `respawn_min_delay` INT(11) NOT NULL DEFAULT 86400,
  `respawn_max_delay` INT(11) NOT NULL DEFAULT 129600,
  `respawn_time` BIGINT NOT NULL DEFAULT 0,
  `currentHp` decimal(8,0) DEFAULT NULL,
  `currentMp` decimal(8,0) DEFAULT NULL,
  PRIMARY KEY (`boss_id`)
) DEFAULT CHARSET=utf8;

-- This table stores spawn infos for all L2 bosses not currently managed by a lair instance.

INSERT IGNORE INTO `grandboss_spawnlist` VALUES
(29001, -21610, 181594, -5734, 0, 86400, 129600, 0, 229898, 667),       -- Queen Ant (40)
(29006, 17726, 108915, -6480, 0, 86400, 129600, 0, 622493, 575),        -- Core (50)
(29014, 55024, 17368, -5412, 10126, 86400, 129600, 0, 622493, 1660),    -- Orfen (50)
(29022, 55312, 219168, -3223, 0, 86400, 129600, 0, 858518, 1975),       -- Zaken (60)
(22215, 24767, -12441, -2532, 15314, 86400, 129600, 0, 340753, 2339),   -- Tyrannosaurus (80)
-- (22215, 28263, -17486, -2539, 50052, 86400, 129600, 0, 340753, 2339),-- Tyrannosaurus (80) -- TODO: Multiple instances per ID
-- (22215, 18229, -17975, -3219, 65140, 86400, 129600, 0, 340753, 2339),-- Tyrannosaurus (80)
(22216, 19897, -9087, -2781, 2686,86400, 129600, 0, 340753, 2339),      -- Tyrannosaurus (80)
(22217, 22827, -14698, -3080, 53946,86400, 129600, 0, 340753, 2339),    -- Tyrannosaurus (80)
-- (29045, 0, 0, 0, 0, 0, 1216600, 11100, 0), -- Frintezza (85)
-- (29046, 0, 0, 0, 0, 0, 1824900, 23310, 0), -- Scarlet Van Halisha (85)
-- (29047, 174238, -89792, -5002, 0, 0, 898044, 4519, 0), -- Scarlet Van Halisha (85)
-- (29099, 0, 0, 0, 0, 0, 1703893, 111000, 0), -- Baylor (83)
-- (29118, 0, 0, 0, 0, 0, 94800, 1110000, 0), -- Beleth (87)
-- (29150, 0, 0, 0, 0, 0, 8727677, 204995, 0), -- Ekimus (?)
-- (29151, 0, 0, 0, 0, 0, 6690, 204995, 0), -- Feral (?)
-- (29152, 0, 0, 0, 0, 0, 6690, 204995, 0), -- Feral (?)
-- (29163, 0, 0, 0, 0, 0, 8727677, 204995, 0), -- Tiat (87)