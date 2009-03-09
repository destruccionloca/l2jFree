-- ----------------------------------
-- Table structure for buff_templates
-- ----------------------------------
DROP TABLE IF EXISTS `buff_templates`;
CREATE TABLE `buff_templates` (
  `id` int(11) unsigned NOT NULL,
  `name` varchar(35) NOT NULL DEFAULT '',
  `skill_id` int(10) unsigned NOT NULL,
  `skill_name` varchar(35) DEFAULT NULL,
  `skill_level` int(10) unsigned NOT NULL DEFAULT '1',
  `skill_force` int(1) NOT NULL DEFAULT '1',
  `skill_order` int(10) unsigned NOT NULL,
  `char_min_level` int(10) unsigned NOT NULL DEFAULT '0',
  `char_max_level` int(10) unsigned NOT NULL DEFAULT '0',
  `char_race` int(1) unsigned NOT NULL DEFAULT '0',
  `char_class` int(1) NOT NULL DEFAULT '0',
  `char_faction` int(10) unsigned NOT NULL DEFAULT '0',
  `price_adena` int(10) unsigned NOT NULL DEFAULT '0',
  `price_points` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`name`,`skill_order`)
) DEFAULT CHARSET=utf8;

--
-- id - template id, not zero value
-- name - template name, for easer access thru html links
-- Note: 'SupportMagic' is reserved name for Newbie Helper buff template
--
-- skill_id, skill_level - skill for buff
-- skill_force - force skill cast, eve if same skill effect present
-- skill_order - order of skill in template, not zero value
-- skill_name - name of skill
--
-- player condtitions for buff: 
-- 
-- char_min_level 
-- char_max_level - minimum and maximum level of character
--
-- char_race - race mask of character
-- 16 - human
-- 8  - elf
-- 4  - dark elf
-- 2  - orc
-- 1  - dwarf
-- example: if set 16+8+1, this mean 25, 
-- then buff can land only on humans,elfs and dwarves
-- set 0 or 31 for all races
--
-- char_class
-- 2 - magus
-- 1 - fighter
-- 0 or 3 - all classes
--
-- char_faction - faction id, 0 - for all or no faction
--
-- price_adena - amount of adena for buff
-- price_points - amount of faction poins for buff

INSERT INTO `buff_templates` VALUES 
(1, 'SupportMagic', 4322, 'WindWalk', 1, 1, 1, 6, 62, 0, 0, 0, 0, 0),
(1, 'SupportMagic', 4323, 'Shield', 1, 1, 2, 6, 62, 0, 0, 0, 0, 0),
(1, 'SupportMagic', 4338, 'Life Cubic', 1, 1, 3, 16, 34, 0, 0, 0, 0, 0),
(1, 'SupportMagic', 4324, 'Bless the Body', 1, 1, 4, 6, 62, 0, 1, 0, 0, 0),
(1, 'SupportMagic', 4325, 'Vampiric Rage', 1, 1, 5, 6, 62, 0, 1, 0, 0, 0),
(1, 'SupportMagic', 4326, 'Regeneration', 1, 1, 6, 6, 62, 0, 1, 0, 0, 0),
(1, 'SupportMagic', 4327, 'Haste', 1, 1, 7, 6, 39, 0, 1, 0, 0, 0),
(1, 'SupportMagic', 5632, 'Haste', 1, 1, 8, 40, 62, 0, 1, 0, 0, 0),
(1, 'SupportMagic', 4328, 'Bless the Soul', 1, 1, 9, 6, 62, 0, 2, 0, 0, 0),
(1, 'SupportMagic', 4329, 'Acumen', 1, 1, 10, 6, 62, 0, 2, 0, 0, 0),
(1, 'SupportMagic', 4330, 'Concentration', 1, 1, 11, 6, 62, 0, 2, 0, 0, 0),
(1, 'SupportMagic', 4331, 'Empower', 1, 1, 12, 6, 62, 0, 2, 0, 0, 0),
(1, 'SupportMagic', 5637, 'Magic Barrier', 1, 1, 13, 6, 62, 0, 0, 0, 0, 0);