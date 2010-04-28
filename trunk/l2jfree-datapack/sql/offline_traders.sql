-- Table to keep offline traders
-- author hex1r0
DROP TABLE IF EXISTS `offline_traders`;
CREATE TABLE `offline_traders` (
  `char_id` INT NOT NULL ,
  `mode` VARCHAR( 1 ) NOT NULL ,
  `msg` VARCHAR( 255 ) NULL ,
  PRIMARY KEY ( `char_id` )
) DEFAULT CHARSET=utf8;