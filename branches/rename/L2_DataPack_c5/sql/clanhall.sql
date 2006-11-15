-- 
-- Table structure for table `clanhall`
-- 

CREATE TABLE IF NOT EXISTS `clanhall` (
  `id` int(11) NOT NULL default '0',
  `name` varchar(40) NOT NULL default '',
  `ownerId` int(11) NOT NULL default '0',
  `lease` int(10) NOT NULL default '0',
  `desc` text NOT NULL,
  `location` varchar(15) NOT NULL default '',
  PRIMARY KEY  (`id`,`name`),
  KEY `id` (`id`)
);

-- 
-- Dumping data for table `clanhall`
-- 

INSERT INTO `clanhall` VALUES
('21', 'Fortress of Resistance', '0', '500000', 'Ol Mahum Fortress of Resistance', 'Dion'),
('22', 'Moonstone Hall', '0', '500000', 'Clan hall located in the Town of Gludio', 'Gludio'),
('23', 'Onyx Hall', '0', '500000', 'Clan hall located in the Town of Gludio', 'Gludio'),
('24', 'Topaz Hall', '0', '500000', 'Clan hall located in the Town of Gludio', 'Gludio'),
('25', 'Ruby Hall', '0', '500000', 'Clan hall located in the Town of Gludio', 'Gludio'),
('26', 'Crystal Hall', '0', '500000', 'Clan hall located in Gludin Village', 'Gludin'),
('27', 'Onyx Hall', '0', '500000', 'Clan hall located in Gludin Village', 'Gludin'),
('28', 'Sapphire Hall', '0', '500000', 'Clan hall located in Gludin Village', 'Gludin'),
('29', 'Moonstone Hall', '0', '500000', 'Clan hall located in Gludin Village', 'Gludin'),
('30', 'Emerald Hall', '0', '500000', 'Clan hall located in Gludin Village', 'Gludin'),
('31', 'The Atramental Barracks', '0', '500000', 'Clan hall located in the Town of Dion', 'Dion'),
('32', 'The Scarlet Barracks', '0', '500000', 'Clan hall located in the Town of Dion', 'Dion'),
('33', 'The Viridian Barracks', '0', '500000', 'Clan hall located in the Town of Dion', 'Dion'),
('34', 'Devastated Castle', '0', '500000', 'Contestable Clan Hall', 'Aden'),
('35', 'Bandit Stronghold', '0', '500000', 'Contestable Clan Hall', 'Oren'),
('36', 'The Golden Chamber', '0', '500000', 'Clan hall located in the Town of Aden', 'Aden'),
('37', 'The Silver Chamber', '0', '500000', 'Clan hall located in the Town of Aden', 'Aden'),
('38', 'The Mithril Chamber', '0', '500000', 'Clan hall located in the Town of Aden', 'Aden'),
('39', 'Silver Manor', '0', '500000', 'Clan hall located in the Town of Aden', 'Aden'),
('40', 'Gold Manor', '0', '500000', 'Clan hall located in the Town of Aden', 'Aden'),
('41', 'The Bronze Chamber', '0', '500000', 'Clan hall located in the Town of Aden', 'Aden'),
('42', 'The Golden Chamber', '0', '500000', 'Clan hall located in the Town of Giran', 'Giran'),
('43', 'The Silver Chamber', '0', '500000', 'Clan hall located in the Town of Giran', 'Giran'),
('44', 'The Mithril Chamber', '0', '500000', 'Clan hall located in the Town of Giran', 'Giran'),
('45', 'The Bronze Chamber', '0', '500000', 'Clan hall located in the Town of Giran', 'Giran'),
('46', 'Silver Manor', '0', '500000', 'Clan hall located in the Town of Giran', 'Giran'),
('47', 'Moonstone Hall', '0', '500000', 'Clan hall located in the Town of Goddard', 'Goddard'),
('48', 'Onyx Hall', '0', '500000', 'Clan hall located in the Town of Goddard', 'Goddard'),
('49', 'Emerald Hall', '0', '500000', 'Clan hall located in the Town of Goddard', 'Goddard'),
('50', 'Sapphire Hall', '0', '500000', 'Clan hall located in the Town of Goddard', 'Goddard'),
('51', 'Mont Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('52', 'Astaire Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('53', 'Aria Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('54', 'Yiana Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('55', 'Roien Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('56', 'Luna Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('57', 'Traban Chamber', '0', '500000', 'An upscale Clan hall located in the Rune Township', 'Rune'),
('58', 'Eisen Hall', '0', '500000', 'Clan hall located in the Town of Schuttgart', 'Schuttgart'),
('59', 'Heavy Metal Hall', '0', '500000', 'Clan hall located in the Town of Schuttgart', 'Schuttgart'),
('60', 'Molten Ore Hall', '0', '500000', 'Clan hall located in the Town of Schuttgart', 'Schuttgart'),
('61', 'Titan Hall', '0', '500000', 'Clan hall located in the Town of Schuttgart', 'Schuttgart'),
('62', 'Rainbow Springs', '0', '500000', '', 'Goddard'),
('63', 'Beast Farm', '0', '500000', '', 'Rune'),
('64', 'Fortress of the Dead', '0', '500000', '', 'Rune');
