CREATE TABLE IF NOT EXISTS `character_birthdays` (
  `charId` INT UNSIGNED NOT NULL,
  `lastClaim` SMALLINT(4) UNSIGNED NOT NULL,
  `year` SMALLINT(4) UNSIGNED NOT NULL, -- stored just in case
  `month` TINYINT(2) UNSIGNED NOT NULL,
  `day` TINYINT(2) UNSIGNED NOT NULL,
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8;