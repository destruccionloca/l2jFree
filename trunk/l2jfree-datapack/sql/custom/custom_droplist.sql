DROP TABLE IF EXISTS `custom_droplist`;
CREATE TABLE `custom_droplist` AS
  SELECT * FROM `droplist` WHERE FALSE;
