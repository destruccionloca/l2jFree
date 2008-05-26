DROP TABLE IF EXISTS `castle_manor_procure`;
CREATE TABLE `castle_manor_procure` (
  `castle_id` int(11) NOT NULL default '0',
  `crop_id` int(11) NOT NULL default '0',
  `can_buy` int(11) NOT NULL default '0',
  `reward_type` int(11) NOT NULL default '0',
  PRIMARY KEY  (`castle_id`,`crop_id`,`reward_type`)
) DEFAULT CHARSET=utf8;


INSERT INTO `castle_manor_procure` VALUES (1, 5122, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5108, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5124, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5120, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5115, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5105, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5117, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5098, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5101, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5099, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5100, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5103, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5095, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5097, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5107, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 6541, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5855, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5845, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5856, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5857, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5853, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5846, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5854, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 6548, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5839, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5843, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5842, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5844, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5838, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5840, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5841, 1000, 2);
INSERT INTO `castle_manor_procure` VALUES (1, 5847, 1000, 2);
