drop table if exists couples;
-- ----------------------------
-- Table structure for couples
-- ----------------------------
CREATE TABLE IF NOT EXISTS `couples` (
  `id` INT auto_increment,
  `player1Id` INT NOT NULL,
  `player2Id` INT NOT NULL,
  `maried` varchar(5) default NULL,
  `affiancedDate` DECIMAL(20,0) DEFAULT 0,

  `weddingDate` DECIMAL(20,0) DEFAULT 0,

   PRIMARY KEY (`id`)
) ENGINE=MyISAM;