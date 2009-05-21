CREATE TABLE IF NOT EXISTS `custom_armor` (
  `item_id` int(11) NOT NULL default '0',
  `item_display_id` int(11) NOT NULL default '0',
  `name` varchar(70) default NULL,
  `bodypart` varchar(15) NOT NULL default '',
  `crystallizable` varchar(5) NOT NULL default '',
  `armor_type` varchar(5) NOT NULL default '',
  `weight` int(5) NOT NULL default '0',
  `material` varchar(15) NOT NULL default '',
  `crystal_type` varchar(4) NOT NULL default '',
  `avoid_modify` int(1) NOT NULL default '0',
  `duration` int(3) NOT NULL default '0',
  `p_def` int(3) NOT NULL default '0',
  `m_def` int(2) NOT NULL default '0',
  `mp_bonus` int(3) NOT NULL default '0',
  `price` int(11) NOT NULL default '0',
  `crystal_count` int(4) default NULL,
  `sellable` varchar(5) default NULL,
  `dropable` varchar(5) NOT NULL default 'true',
  `destroyable` varchar(5) NOT NULL default 'true',
  `tradeable` varchar(5) NOT NULL default 'true',
  `item_skill_id` decimal(11,0) NOT NULL default '0',
  `item_skill_lvl` decimal(11,0) NOT NULL default '0',
  PRIMARY KEY (`item_id`)
) DEFAULT CHARSET=utf8;

ALTER TABLE `custom_armor` ADD `races` VARCHAR(20) NOT NULL DEFAULT '-1';
ALTER TABLE `custom_armor` ADD `classes` VARCHAR(255) NOT NULL DEFAULT '-1';
ALTER TABLE `custom_armor` ADD `sex` INT (1) NOT NULL DEFAULT -1;

ALTER TABLE `custom_armor`
CHANGE `item_skill_id` `item_skill_id` VARCHAR(60) NOT NULL DEFAULT '0',
CHANGE `item_skill_lvl` `item_skill_lvl` VARCHAR(30) NOT NULL DEFAULT '0';