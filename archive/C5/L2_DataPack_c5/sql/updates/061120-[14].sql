-- ----------------------------
-- Table structure for itemsonground
-- ----------------------------
CREATE TABLE IF NOT EXISTS `itemsonground` (
  `object_id` int(11) NOT NULL default '0',
  `item_id` int(11) default NULL,
  `count` int(11) default NULL,
  `enchant_level` int(11) default NULL,
  `x` int(11) NOT NULL default '0',
  `y` int(11) NOT NULL default '0',
  `z` int(11) NOT NULL default '0',
  `drop_time` int(11) NOT NULL default '0',
  PRIMARY KEY  (`object_id`)
);

-- ----------------------------
-- Table structure for zariche
-- ----------------------------
CREATE TABLE IF NOT EXISTS `zariche` (
  `playerId` INT,
  `playerKarma` INT DEFAULT 0,
  `playerPkKills` INT DEFAULT 0,
  `nbKills` INT DEFAULT 0,
  `endTime` DECIMAL(20,0) DEFAULT 0,
  PRIMARY KEY (`playerId`)
);