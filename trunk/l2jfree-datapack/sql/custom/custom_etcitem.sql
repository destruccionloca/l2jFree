DROP TABLE IF EXISTS `custom_etcitem`;
CREATE TABLE `custom_etcitem` AS
  SELECT * FROM `etcitem` WHERE FALSE;

ALTER TABLE `custom_etcitem`
  ADD COLUMN `item_display_id` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0 AFTER `item_id`;
