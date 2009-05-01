-- -------------------------------------------
-- Table structure for table `custom_etcitem`
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `custom_etcitem` (
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_display_id` decimal(11,0) NOT NULL DEFAULT '0',
  `name` varchar(100) NOT NULL DEFAULT '',
  `crystallizable` varchar(5) NOT NULL DEFAULT 'false',
  `item_type` varchar(14) NOT NULL DEFAULT 'none',
  `weight` int(5) NOT NULL DEFAULT '0',
  `consume_type` varchar(9) NOT NULL DEFAULT 'normal',
  `material` varchar(15) NOT NULL DEFAULT 'wood',
  `crystal_type` varchar(4) NOT NULL DEFAULT 'none',
  `duration` int(3) NOT NULL DEFAULT '-1',          -- duration for shadow items
  `time` int(4) NOT NULL DEFAULT '-1',              -- duration for time limited items
  `price` int(11) NOT NULL DEFAULT '0',
  `crystal_count` int(4) NOT NULL DEFAULT '0',
  `sellable` varchar(5) NOT NULL DEFAULT 'false',
  `dropable` varchar(5) NOT NULL DEFAULT 'false',
  `destroyable` varchar(5) NOT NULL DEFAULT 'true',
  `tradeable` varchar(5) NOT NULL DEFAULT 'false',
  `skill` varchar(70) NOT NULL DEFAULT '',
  `html` varchar(5) NOT NULL DEFAULT 'false',
  PRIMARY KEY (`item_id`)
) DEFAULT CHARSET=utf8;