-- -----------------------------------------
-- Table structure for table `custom_armor`
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS `custom_armor` (
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_display_id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(100) NOT NULL DEFAULT '',
  `bodypart` varchar(15) NOT NULL DEFAULT 'none',
  `crystallizable` varchar(5) NOT NULL DEFAULT 'false',
  `armor_type` varchar(5) NOT NULL DEFAULT 'none',
  `weight` int(5) NOT NULL DEFAULT '0',
  `material` varchar(15) NOT NULL DEFAULT 'wood',
  `crystal_type` varchar(4) NOT NULL DEFAULT 'none',
  `avoid_modify` int(1) NOT NULL DEFAULT '0',
  `duration` int(3) NOT NULL DEFAULT '-1',          -- duration for shadow items
  `time` int(4) NOT NULL DEFAULT '-1',              -- duration for time limited items
  `p_def` int(3) NOT NULL DEFAULT '0',
  `m_def` int(2) NOT NULL DEFAULT '0',
  `mp_bonus` int(3) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `crystal_count` int(4) NOT NULL DEFAULT '0',
  `sellable` varchar(5) NOT NULL DEFAULT 'false',
  `dropable` varchar(5) NOT NULL DEFAULT 'false',
  `destroyable` varchar(5) NOT NULL DEFAULT 'true',
  `tradeable` varchar(5) NOT NULL DEFAULT 'false',
  `skills_item` varchar(70) NOT NULL DEFAULT '',
  PRIMARY KEY (`item_id`)
) DEFAULT CHARSET=utf8;
