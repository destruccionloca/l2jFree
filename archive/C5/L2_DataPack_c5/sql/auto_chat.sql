DROP TABLE IF EXISTS `auto_chat`;
CREATE TABLE `auto_chat` (
  `groupId` int(11) NOT NULL default '0',
  `groupName` varchar(128) NOT NULL,
  `npcId` int(11) NOT NULL default '0',
  `chatDelay` bigint(20) NOT NULL default '-1',
  `chatRange` smallint(6) NOT NULL default '-1',
  `chatRandom` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`groupId`)
) DEFAULT CHARSET=utf8;

INSERT INTO `auto_chat` VALUES 
(1, '', 31093, -1, -1, 0),
(2, '', 31094, -1, -1, 0);
