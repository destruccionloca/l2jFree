DROP TABLE IF EXISTS `hb_naia_spawnlist`;
CREATE TABLE `hb_naia_spawnlist` (
  `npc_id` int(11) NOT NULL,
  `locx` int(11) NOT NULL,
  `locy` int(11) NOT NULL,
  `locz` int(11) NOT NULL,
  `heading` int(11) NOT NULL,
  `respawn_delay` int(11) NOT NULL,
  `room_id` tinyint(1) NOT NULL
) DEFAULT CHARSET=utf8;