DROP TABLE IF EXISTS `custom_spawnlist`;
CREATE TABLE `custom_spawnlist` AS
  SELECT * FROM `spawnlist` WHERE FALSE;
