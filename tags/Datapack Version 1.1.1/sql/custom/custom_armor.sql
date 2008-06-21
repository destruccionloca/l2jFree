CREATE TABLE IF NOT EXISTS `custom_armor` (
  `item_id` int(11) NOT NULL DEFAULT '0',
  `item_display_id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(70) DEFAULT NULL,
  `bodypart` varchar(15) NOT NULL DEFAULT '',
  `crystallizable` varchar(5) NOT NULL DEFAULT '',
  `armor_type` varchar(5) NOT NULL DEFAULT '',
  `weight` int(5) NOT NULL DEFAULT '0',
  `material` varchar(15) NOT NULL DEFAULT '',
  `crystal_type` varchar(4) NOT NULL DEFAULT '',
  `avoid_modify` int(1) NOT NULL DEFAULT '0',
  `duration` int(3) NOT NULL DEFAULT '0',
  `p_def` int(3) NOT NULL DEFAULT '0',
  `m_def` int(2) NOT NULL DEFAULT '0',
  `mp_bonus` int(3) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `crystal_count` int(4) DEFAULT NULL,
  `sellable` varchar(5) DEFAULT NULL,
  `dropable` varchar(5) NOT NULL DEFAULT 'true',
  `destroyable` varchar(5) NOT NULL DEFAULT 'true',
  `tradeable` varchar(5) NOT NULL DEFAULT 'true',
  `skills_item` varchar(70) NOT NULL DEFAULT '',
  `races` VARCHAR(20) NOT NULL DEFAULT '-1',
  `classes` VARCHAR(255) NOT NULL DEFAULT '-1',
  `sex` INT (1) NOT NULL DEFAULT -1,
  PRIMARY KEY (`item_id`)
) DEFAULT CHARSET=utf8;


-- Agathion Seal Bracelet - Little Angel

INSERT IGNORE custom_armor (`item_id`, `name`, `bodypart`, `crystallizable`, `armor_type`, `weight`, `material`, `crystal_type`, `avoid_modify`, `duration`, `p_def`, `m_def`, `mp_bonus`, `price`, `crystal_count`, `sellable`, `dropable`, `destroyable`, `tradeable`, `skills_item`, `races`, `classes`, `sex`) VALUES
(10316, 'Agathion Seal Bracelet - Little Angel', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3423-1;3267-1', '-1', '-1', -1),
(10317, 'Agathion Seal Bracelet - Little Angel', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3423-1;3267-1', '-1', '-1', -1),
(10318, 'Agathion Seal Bracelet - Little Angel', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3423-1;3267-1', '-1', '-1', -1),
(10319, 'Agathion Seal Bracelet - Little Angel', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3423-1;3267-1', '-1', '-1', -1),
(10320, 'Agathion Seal Bracelet - Little Angel', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3423-1;3267-1', '-1', '-1', -1);

-- Agathion Seal Bracelet - Little Devil

INSERT IGNORE custom_armor (`item_id`, `name`, `bodypart`, `crystallizable`, `armor_type`, `weight`, `material`, `crystal_type`, `avoid_modify`, `duration`, `p_def`, `m_def`, `mp_bonus`, `price`, `crystal_count`, `sellable`, `dropable`, `destroyable`, `tradeable`, `skills_item`, `races`, `classes`, `sex`) VALUES
(10322, 'Agathion Seal Bracelet - Little Devil', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3424-1;3267-1', '-1', '-1', -1),
(10323, 'Agathion Seal Bracelet - Little Devil', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3424-1;3267-1', '-1', '-1', -1),
(10324, 'Agathion Seal Bracelet - Little Devil', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3424-1;3267-1', '-1', '-1', -1),
(10325, 'Agathion Seal Bracelet - Little Devil', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3424-1;3267-1', '-1', '-1', -1);

-- Agathion Seal Bracelet - Rudolph

INSERT IGNORE custom_armor (`item_id`, `name`, `bodypart`, `crystallizable`, `armor_type`, `weight`, `material`, `crystal_type`, `avoid_modify`, `duration`, `p_def`, `m_def`, `mp_bonus`, `price`, `crystal_count`, `sellable`, `dropable`, `destroyable`, `tradeable`, `skills_item`, `races`, `classes`, `sex`) VALUES
(10607, 'Agathion Seal Bracelet - Rudolph', 'lbracelet', 'true', 'none', 150, 'wood', 'none', 0, -1, 0, 0, 0, 0, 0, 'true', 'true', 'true', 'true', '3425-1;3267-1', '-1', '-1', -1);