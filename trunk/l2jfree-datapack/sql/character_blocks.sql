-- ----------------------------
-- Table structure for `character_blocks`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `character_blocks` (
  `charId` INT UNSIGNED NOT NULL,
  `name` VARCHAR(35) NOT NULL,
  PRIMARY KEY (`char_id`,`name`)
) DEFAULT CHARSET=utf8;