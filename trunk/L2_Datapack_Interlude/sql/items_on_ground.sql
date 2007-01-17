-- ----------------------------
-- Table structure for itemsonground
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ItemsOnGround` (
  `object_id` int(11) NOT NULL default '0',
  `item_id` int(11) default NULL,
  `count` int(11) default NULL,
  `enchant_level` int(11) default NULL,
  `x` int(11) NOT NULL default '0',
  `y` int(11) NOT NULL default '0',
  `z` int(11) NOT NULL default '0',
  `drop_time` int(11) NOT NULL default '0',
  PRIMARY KEY  (`object_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;