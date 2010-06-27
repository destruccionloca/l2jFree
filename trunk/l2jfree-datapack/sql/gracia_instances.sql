CREATE TABLE IF NOT EXISTS `gracia_instances` (
  `variable` varchar(50) NOT NULL,
  `value` bigint(15) NOT NULL,
  PRIMARY KEY (`variable`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `gracia_instances` (`variable`, `value`) VALUES
('sod_state', 1),
('sod_tiatKills', 0),
('sod_defenseSwitch', 0);
