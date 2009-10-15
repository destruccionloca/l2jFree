DROP TABLE IF EXISTS `custom_weapon`;
CREATE TABLE `custom_weapon` AS
  SELECT * FROM `weapon` WHERE FALSE;

ALTER TABLE `custom_weapon`
  ADD COLUMN `item_display_id` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0 AFTER `item_id`;
