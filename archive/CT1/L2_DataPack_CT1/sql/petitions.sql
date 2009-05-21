-- ---------------------------
-- Table structure for table `petitions`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `petitions` (
  `petition_id` int(11) NOT NULL auto_increment,
  `char_id` int(11) NOT NULL default '0',
  `petition_txt` text NOT NULL,
  `status` varchar(255) NOT NULL default 'New',
  PRIMARY KEY (`petition_id`)
) DEFAULT CHARSET=utf8;