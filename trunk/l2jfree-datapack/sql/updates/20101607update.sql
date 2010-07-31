---- Sync L2jdp 6776

---- 20100118
ALTER TABLE  `castle` ADD `showNpcCrest` enum('true','false') DEFAULT 'false' NOT  NULL AFTER `regTimeEnd`;

---- 20091202
ALTER TABLE  `characters` ADD `createTime` DECIMAL(20,0) NOT  NULL default  '0' AFTER `vitality_points`;

---- 20091207
ALTER TABLE  `characters` ADD `title_color` MEDIUMINT UNSIGNED NOT NULL default  '16777079' AFTER `title`;

---- 20091210
ALTER TABLE `custom_armor` ADD `additionalname` varchar(120) NOT  NULL default  '' AFTER `name`;
ALTER TABLE `custom_etcitem` ADD `additionalname` varchar(100) NOT NULL default '' AFTER `name`;
ALTER TABLE `custom_weapon` ADD `additionalname` varchar(120) NOT NULL default '' AFTER `name`;
ALTER TABLE `custom_npc` ADD `enchant` INT NOT NULL default 0 after `armor`;

---- 20091216
UPDATE `items` SET `loc` = 'WAREHOUSE'  WHERE `loc` = 'FREIGHT';