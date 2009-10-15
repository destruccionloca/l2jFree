DROP TABLE IF EXISTS `custom_armor`;
CREATE TABLE `custom_armor` AS
  SELECT * FROM `armor` WHERE FALSE;

ALTER TABLE `custom_armor`
  ADD COLUMN `item_display_id` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0 AFTER `item_id`;
