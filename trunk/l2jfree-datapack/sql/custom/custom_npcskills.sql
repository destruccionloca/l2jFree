DROP TABLE IF EXISTS `custom_npcskills`;
CREATE TABLE `custom_npcskills` AS
  SELECT * FROM `npcskills` WHERE FALSE;
