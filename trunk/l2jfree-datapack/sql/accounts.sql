-- ---------------------------
-- Table structure for accounts
-- ---------------------------
CREATE TABLE IF NOT EXISTS `accounts` (
  `login` VARCHAR(45) NOT NULL DEFAULT '',
  `password` VARCHAR(45) ,
  `lastactive` DECIMAL(20),
  `accessLevel` INT NOT NULL DEFAULT '0',
  `lastServerId` INT NOT NULL DEFAULT '0',
  `lastIP` VARCHAR(20),
  PRIMARY KEY (`login`)
) DEFAULT CHARSET=utf8;
