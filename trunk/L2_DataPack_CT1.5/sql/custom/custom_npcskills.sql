-- ---------------------------
-- Table structure for table `custom_npcskills`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `custom_npcskills` (
  `npcid` int(11) NOT NULL DEFAULT '0',
  `skillid` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`npcid`,`skillid`,`level`)
) DEFAULT CHARSET=utf8;