-- ---------------------------
-- Table structure for table `petitions`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `petitions` (
  `petition_id` int(11) NOT NULL auto_increment,
  `charId` int(11) NOT NULL DEFAULT '0',
  `petition_txt` text NOT NULL,
  `status` varchar(255) NOT NULL DEFAULT 'New',
  PRIMARY KEY (`petition_id`)
) DEFAULT CHARSET=utf8;