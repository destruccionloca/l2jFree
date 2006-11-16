-- ----------------------------
-- Table structure for zariche
-- ----------------------------
CREATE TABLE IF NOT EXISTS zariche (
  `playerId` INT,
  `playerKarma` INT DEFAULT 0,
  `playerPkKills` INT DEFAULT 0,
  `nbKills` INT DEFAULT 0,
  `endTime` DECIMAL(20,0) DEFAULT 0,
  PRIMARY KEY (`playerId`)
);