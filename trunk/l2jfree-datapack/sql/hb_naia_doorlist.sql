DROP TABLE IF EXISTS `hb_naia_doorlist`;
CREATE TABLE `hb_naia_doorlist` (
  `door_id` int(11) NOT NULL,
  `action_order` tinyint(1) NOT NULL,
  `room_id` int(11) NOT NULL
) DEFAULT CHARSET=utf8;