-- ---------------------------
-- Table structure for fort_door
-- ---------------------------
DROP TABLE IF EXISTS `fort_door`;
CREATE TABLE `fort_door` (
  `fortId` int(11) NOT NULL,
  `id` int(10) unsigned NOT NULL default '0',
  `name` varchar(128) collate utf8_unicode_ci NOT NULL default '',
  `hp` int(10) unsigned NOT NULL default '0',
  `pdef` int(10) unsigned NOT NULL default '0',
  `mdef` int(10) unsigned NOT NULL default '0',
  `posx` int(11) NOT NULL default '0',
  `posy` int(11) NOT NULL default '0',
  `posz` int(11) NOT NULL default '0',
  `ax` int(11) NOT NULL default '0',
  `ay` int(11) NOT NULL default '0',
  `bx` int(11) NOT NULL default '0',
  `by` int(11) NOT NULL default '0',
  `cx` int(11) NOT NULL default '0',
  `cy` int(11) NOT NULL default '0',
  `dx` int(11) NOT NULL default '0',
  `dy` int(11) NOT NULL default '0',
  `minz` int(11) NOT NULL default '0',
  `maxz` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) DEFAULT CHARSET=utf8;

-- Dragonspine doors
INSERT INTO `fort_door` (`fortId`, `id`, `name`, `hp`, `pdef`, `mdef`, `posx`, `posy`, `posz`, `ax`, `ay`, `bx`, `by`, `cx`, `cy`, `dx`, `dy`, `minz`, `maxz`) VALUES
(115, 20200001, 'L2fortress_02_s.fort02_wall_door', 158250, 644, 518, 12503, 93513, -3475, 12383, 93568, 12383, 93463, 12611, 93470, 12611, 93567, -3475, -3475),
(115, 20200002, 'L2fortress_02_s.Fort02_indoor', 158250, 644, 518, 11459, 94386, -3454, 11543, 95594, 11544, 95578, 11459, 95580, 11459, 95595, -3454, -3454),
(115, 20200003, 'L2fortress_02_s.Fort02_indoor', 158250, 644, 518, 11615, 94386, -3454, 11530, 95593, 11530, 95578, 11626, 95578, 11627, 95597, -3454, -3454),
(115, 20200004, 'L2fortress_02_s.Fort02_indoor', 158250, 644, 518, 10128, 94938, -3426, 10121, 94924, 10133, 94925, 10137, 95024, 10120, 95024, -3426, -3426),
(115, 20200005, 'L2fortress_02_s.Fort02_indoor', 158250, 644, 518, 10128, 95094, -3426, 10136, 95006, 10120, 95006, 10122, 95104, 10137, 95105, -3426, -3426),
(115, 20200006, 'L2fortress_02_s.Fort02_indoor', 158250, 644, 518, 11459, 95587, -3454, 11543, 94393, 11544, 94377, 11459, 94379, 11459, 94394, -3454, -3454),
(115, 20200007, 'L2fortress_02_s.Fort02_indoor', 158250, 644, 518, 11615, 95587, -3454, 11530, 94392, 11530, 94377, 11626, 94377, 11627, 94396, -3454, -3454),
(115, 20200008, 'L2fortress_02_s.fort02_wall_door', 158250, 644, 518, 10493, 96565, -3475, 10373, 96620, 10373, 96515, 10601, 96522, 10601, 96619, -3475, -3475);