-- ------------------------------------------
-- Table structure for table `custom_weapon`
-- ------------------------------------------
CREATE TABLE IF NOT EXISTS `custom_weapon` (
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_display_id` decimal(11,0) NOT NULL DEFAULT '0',
  `name` varchar(100) NOT NULL DEFAULT '',
  `bodypart` varchar(15) NOT NULL DEFAULT '',
  `crystallizable` varchar(5) NOT NULL DEFAULT 'false',
  `weight` int(5) NOT NULL DEFAULT '0',
  `soulshots` decimal(2,0) NOT NULL DEFAULT '0',
  `spiritshots` decimal(1,0) NOT NULL DEFAULT '0',
  `material` varchar(15) NOT NULL DEFAULT 'wood',
  `crystal_type` varchar(4) NOT NULL DEFAULT 'none',
  `p_dam` decimal(5,0) NOT NULL DEFAULT '0',
  `rnd_dam` decimal(2,0) NOT NULL DEFAULT '0',
  `weaponType` varchar(8) NOT NULL DEFAULT '',
  `critical` decimal(2,0) NOT NULL DEFAULT '0',
  `hit_modify` decimal(6,5) NOT NULL DEFAULT '0',
  `avoid_modify` decimal(2,0) NOT NULL DEFAULT '0',
  `shield_def` decimal(3,0) NOT NULL DEFAULT '0',
  `shield_def_rate` decimal(2,0) NOT NULL DEFAULT '0',
  `atk_speed` decimal(3,0) NOT NULL DEFAULT '0',
  `mp_consume` decimal(2,0) NOT NULL DEFAULT '0',
  `m_dam` decimal(3,0) NOT NULL DEFAULT '0',
  `duration` int(3) NOT NULL DEFAULT '-1',          -- duration for shadow items
  `time` int(4) NOT NULL DEFAULT '-1',              -- duration for time limited items
  `price` int(11) NOT NULL DEFAULT '0',
  `crystal_count` int(4) NOT NULL DEFAULT '0',
  `sellable` varchar(5) NOT NULL DEFAULT 'false',
  `dropable` varchar(5) NOT NULL DEFAULT 'false',
  `destroyable` varchar(5) NOT NULL DEFAULT 'true',
  `tradeable` varchar(5) NOT NULL DEFAULT 'false',
  `skills_item` varchar(70) NOT NULL DEFAULT '',
  `skills_enchant4` varchar(70) NOT NULL DEFAULT '',
  `skills_onCast` varchar(70) NOT NULL DEFAULT '',
  `skills_onCrit` varchar(70) NOT NULL DEFAULT '',
  `change_weaponId` decimal(11,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`item_id`)
) DEFAULT CHARSET=utf8;
