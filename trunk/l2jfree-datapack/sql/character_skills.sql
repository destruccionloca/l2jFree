-- ------------------------------------
-- Table structure for character_skills
-- ------------------------------------
CREATE TABLE IF NOT EXISTS `character_skills` (
  `charId` INT UNSIGNED NOT NULL,
  `skill_id` SMALLINT UNSIGNED NOT NULL,
  `skill_level` SMALLINT(3) NOT NULL,
  `skill_name` VARCHAR(40),
  `class_index` TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (`charId`,`skill_id`,`class_index`)
) DEFAULT CHARSET=utf8;
