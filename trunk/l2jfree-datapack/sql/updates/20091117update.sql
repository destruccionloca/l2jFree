INSERT IGNORE INTO `character_birthdays` (`charId`, `lastClaim`, `year`, `month`, `day`)
SELECT `charId`, `face`, `face`, `face`, `face` FROM `characters`;
SET @YEAR = (SELECT Year(CURDATE()));
UPDATE `character_birthdays` SET `lastClaim` = @YEAR, `year` = @YEAR, `month` = (SELECT Month(CURDATE())), `day` = (SELECT Day(CURDATE())) WHERE `year` = `month`;