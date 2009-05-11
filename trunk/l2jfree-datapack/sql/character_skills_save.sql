-- ---------------------------
-- Table structure for `character_skills_save`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `character_skills_save` (
  `charId` INT UNSIGNED NOT NULL,
  `skill_id` SMALLINT UNSIGNED NOT NULL,
  `skill_level` SMALLINT NOT NULL,
  `effect_count` INT NOT NULL,
  `effect_cur_time` INT NOT NULL,
  `reuse_delay` INT NOT NULL,
  `systime` BIGINT UNSIGNED NOT NULL,
  `restore_type` TINYINT UNSIGNED NOT NULL,
  `class_index` TINYINT UNSIGNED NOT NULL,
  `buff_index` SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (`charId`,`skill_id`,`class_index`)
) DEFAULT CHARSET=utf8;