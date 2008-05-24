-- --------------------------------
-- Table structure for `tvt_teams`
-- Created by SqueezeD from l2jfree
-- --------------------------------
DROP TABLE IF EXISTS `tvt_teams`;
CREATE TABLE `tvt_teams` (
  `teamId` int(4) NOT NULL DEFAULT '0',
  `teamName` varchar(255) NOT NULL DEFAULT '',
  `teamX` int(11) NOT NULL DEFAULT '0',
  `teamY` int(11) NOT NULL DEFAULT '0',
  `teamZ` int(11) NOT NULL DEFAULT '0',
  `teamColor` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`teamId`)
) DEFAULT CHARSET=utf8;