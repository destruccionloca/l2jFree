ALTER TABLE `character_hennas`
  CHANGE `charId` `charId` INT UNSIGNED NOT NULL,
  CHANGE `symbol_id` `symbol_id` TINYINT UNSIGNED NOT NULL,
  CHANGE `slot` `slot` TINYINT UNSIGNED NOT NULL,
  CHANGE `class_index` `class_index` TINYINT UNSIGNED NOT NULL;

ALTER TABLE `character_shortcuts`
  CHANGE `charId` `charId` INT UNSIGNED NOT NULL,
  CHANGE `slot` `slot` TINYINT NOT NULL,
  CHANGE `page` `page` TINYINT NOT NULL,
  CHANGE `type` `type` TINYINT NOT NULL,
  CHANGE `shortcut_id` `shortcut_id` INT NOT NULL,
  CHANGE `level` `level` TINYINT NOT NULL,
  CHANGE `class_index` `class_index` TINYINT UNSIGNED NOT NULL;

ALTER TABLE `character_skills`
  CHANGE `charId` `charId` INT UNSIGNED NOT NULL,
  CHANGE `skill_id` `skill_id` SMALLINT UNSIGNED NOT NULL,
  CHANGE `skill_level` `skill_level` SMALLINT NOT NULL,
  CHANGE `skill_name` `skill_name` VARCHAR(40),
  CHANGE `class_index` `class_index` TINYINT UNSIGNED NOT NULL;

ALTER TABLE `character_skills_save`
  CHANGE `charId` `charId` INT UNSIGNED NOT NULL,
  CHANGE `skill_id` `skill_id` SMALLINT UNSIGNED NOT NULL,
  CHANGE `skill_level` `skill_level` SMALLINT NOT NULL,
  CHANGE `effect_count` `effect_count` INT NOT NULL,
  CHANGE `effect_cur_time` `effect_cur_time` INT NOT NULL,
  CHANGE `reuse_delay` `reuse_delay` INT NOT NULL,
  CHANGE `systime` `systime` BIGINT UNSIGNED NOT NULL,
  CHANGE `restore_type` `restore_type` TINYINT UNSIGNED NOT NULL,
  CHANGE `class_index` `class_index` TINYINT UNSIGNED NOT NULL,
  CHANGE `buff_index` `buff_index` SMALLINT UNSIGNED NOT NULL;

ALTER TABLE `character_subclasses`
  CHANGE `charId` `charId` INT UNSIGNED NOT NULL,
  CHANGE `class_id` `class_id` TINYINT UNSIGNED NOT NULL,
  CHANGE `exp` `exp` BIGINT UNSIGNED NOT NULL,
  CHANGE `sp` `sp` INT UNSIGNED NOT NULL,
  CHANGE `level` `level` TINYINT UNSIGNED NOT NULL,
  CHANGE `class_index` `class_index` TINYINT UNSIGNED NOT NULL;