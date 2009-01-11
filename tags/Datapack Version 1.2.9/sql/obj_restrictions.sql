CREATE TABLE IF NOT EXISTS `obj_restrictions` (
  `entry_id` int(10) unsigned NOT NULL auto_increment,
  `obj_Id` int(11) unsigned NOT NULL default '0',
  `type` varchar(50) NOT NULL default '',
  `delay` int(11) NOT NULL default '-1',
  `message` varchar(250) default NULL,
  PRIMARY KEY  (`entry_id`)
) DEFAULT CHARSET=utf8;
