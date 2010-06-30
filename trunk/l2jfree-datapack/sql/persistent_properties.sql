CREATE TABLE IF NOT EXISTS `persistent_properties` (
  `class_name` VARCHAR(100) NOT NULL,
  `property_name` VARCHAR(100) NOT NULL,
  `property_value` TEXT NOT NULL,
  PRIMARY KEY (`class_name`,`property_name`)
) DEFAULT CHARSET=utf8;
