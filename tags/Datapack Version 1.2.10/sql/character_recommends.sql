-- ----------------------------
-- Table structure for `character_recommends`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `character_recommends` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `target_id` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`target_id`)
) DEFAULT CHARSET=utf8;