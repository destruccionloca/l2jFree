-- ---------------------------
-- Table structure for `olympiad_nobles`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `olympiad_nobles` (
  `charId` decimal(11,0) NOT NULL DEFAULT '0',
  `class_id` decimal(3,0) NOT NULL DEFAULT '0',
  `char_name` varchar(45) NOT NULL DEFAULT '',
  `olympiad_points` decimal(10,0) NOT NULL DEFAULT '0',
  `competitions_done` decimal(3,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8;