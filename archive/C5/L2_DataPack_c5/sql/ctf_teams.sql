-- --------------------------------
-- Table structure for ctf_teams
-- Created by SqueezeD from l2jfree
-- --------------------------------

DROP TABLE IF EXISTS `ctf_teams`;
CREATE TABLE `ctf_teams` (
  `teamId` int(4) NOT NULL default '0',
  `teamName` varchar(255) NOT NULL default '',
  `flagId` int(11) NOT NULL default '0',
  `flagX` int(11) NOT NULL default '0',
  `flagY` int(11) NOT NULL default '0',
  `flagZ` int(11) NOT NULL default '0',
  `teamColor` int(11) NOT NULL default '0',
  PRIMARY KEY (`teamId`)
) DEFAULT CHARSET=utf8;