CREATE TABLE IF NOT EXISTS `territory_registrations` (
  `castleId` INT(1) NOT NULL DEFAULT '0',
  `registeredId` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`castleId`,`registeredId`)
) DEFAULT CHARSET=utf8;
