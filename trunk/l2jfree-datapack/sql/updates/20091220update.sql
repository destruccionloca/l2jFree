INSERT INTO `character_skill_reuses` (`charId`, `skillId`, `reuseDelay`, `expiration`)
  SELECT `charId`, `skill_id`, `reuse_delay`, `systime` FROM `character_skills_save` WHERE `systime` > UNIX_TIMESTAMP() * 1000;

DELETE FROM `character_skills_save` WHERE `restore_type` = 1;

INSERT INTO `character_effects` (`charId`, `skillId`, `skillLvl`, `count`, `remaining`, `classIndex`, `buffIndex`)
  SELECT `charId`, `skill_id`, `skill_level`, `effect_count`, `effect_cur_time`, `class_index`, `buff_index` FROM `character_skills_save`;

DROP TABLE `character_skills_save`;
