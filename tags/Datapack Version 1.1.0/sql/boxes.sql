-- ---------------------------
-- Table structure for table `boxes`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `boxes` (
  `id` int(11) NOT NULL auto_increment,
  `spawn` decimal(11,0) DEFAULT NULL,
  `npcid` decimal(11,0) DEFAULT NULL,
  `drawer` varchar(32) DEFAULT NULL,
  `itemid` decimal(11,0) DEFAULT NULL,
  `name` varchar(32) DEFAULT '',
  `count` decimal(11,0) DEFAULT NULL,
  `enchant` decimal(2,0) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;