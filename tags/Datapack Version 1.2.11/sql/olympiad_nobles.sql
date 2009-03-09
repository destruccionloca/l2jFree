-- ---------------------------------------
-- Table structure for `olympiad_nobles`
-- ---------------------------------------
CREATE TABLE IF NOT EXISTS `olympiad_nobles` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `class_id` decimal(3,0) NOT NULL DEFAULT 0,
  `olympiad_points` decimal(10,0) NOT NULL DEFAULT 0,
  `competitions_done` decimal(3,0) NOT NULL DEFAULT 0,
  `competitions_won` decimal(3,0) NOT NULL DEFAULT 0,
  `competitions_lost` decimal(3,0) NOT NULL DEFAULT 0,
  `competitions_drawn` decimal(3,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8;