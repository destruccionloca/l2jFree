-- 
-- Table structure for table `auction`
-- 

DROP TABLE IF EXISTS `auction`;
CREATE TABLE `auction` (
  `id` int(11) NOT NULL default '0',
  `sellerId` int(11) NOT NULL default '0',
  `sellerName` varchar(50) NOT NULL default 'NPC',
  `sellerClanName` varchar(50) NOT NULL default '',
  `itemType` varchar(25) NOT NULL default '',
  `itemId` int(11) NOT NULL default '0',
  `itemObjectId` int(11) NOT NULL default '0',
  `itemName` varchar(40) NOT NULL default '',
  `itemQuantity` int(11) NOT NULL default '0',
  `startingBid` int(11) NOT NULL default '0',
  `currentBid` int(11) NOT NULL default '0',
  `endDate` decimal(20,0) NOT NULL default '0',
  PRIMARY KEY  (`itemType`,`itemId`,`itemObjectId`),
  KEY `id` (`id`)
);

-- 
-- Dumping data for table `auction`
-- 

INSERT INTO `auction` VALUES
('22', '0', 'NPC', 'NPC Clan', 'ClanHall', '22', '0', 'Moonstone Hall', '1', '20000000', '0', '1161212400000'),
('23', '0', 'NPC', 'NPC Clan', 'ClanHall', '23', '0', 'Onyx Hall', '1', '20000000', '0', '1161212400000'),
('24', '0', 'NPC', 'NPC Clan', 'ClanHall', '24', '0', 'Topaz Hall', '1', '20000000', '0', '1161212400000'),
('25', '0', 'NPC', 'NPC Clan', 'ClanHall', '25', '0', 'Ruby Hall', '1', '20000000', '0', '1161212400000'),
('26', '0', 'NPC', 'NPC Clan', 'ClanHall', '26', '0', 'Crystal Hall', '1', '20000000', '0', '1161212400000'),
('27', '0', 'NPC', 'NPC Clan', 'ClanHall', '27', '0', 'Onyx Hall', '1', '20000000', '0', '1161212400000'),
('28', '0', 'NPC', 'NPC Clan', 'ClanHall', '28', '0', 'Sapphire Hall', '1', '20000000', '0', '1161212400000'),
('29', '0', 'NPC', 'NPC Clan', 'ClanHall', '29', '0', 'Moonstone Hall', '1', '20000000', '0', '1161212400000'),
('30', '0', 'NPC', 'NPC Clan', 'ClanHall', '30', '0', 'Emerald Hall', '1', '20000000', '0', '1161212400000'),
('31', '0', 'NPC', 'NPC Clan', 'ClanHall', '31', '0', 'The Atramental Barracks', '1', '8000000', '0', '1161212400000'),
('32', '0', 'NPC', 'NPC Clan', 'ClanHall', '32', '0', 'The Scarlet Barracks', '1', '8000000', '0', '1161212400000'),
('33', '0', 'NPC', 'NPC Clan', 'ClanHall', '33', '0', 'The Viridian Barracks', '1', '8000000', '0', '1161212400000'),
('36', '0', 'NPC', 'NPC Clan', 'ClanHall', '36', '0', 'The Golden Chamber', '1', '50000000', '0', '1161212400000'),
('37', '0', 'NPC', 'NPC Clan', 'ClanHall', '37', '0', 'The Silver Chamber', '1', '50000000', '0', '1161212400000'),
('38', '0', 'NPC', 'NPC Clan', 'ClanHall', '38', '0', 'The Mithril Chamber', '1', '50000000', '0', '1161212400000'),
('39', '0', 'NPC', 'NPC Clan', 'ClanHall', '39', '0', 'Silver Manor', '1', '50000000', '0', '1161212400000'),
('40', '0', 'NPC', 'NPC Clan', 'ClanHall', '40', '0', 'Gold Manor', '1', '50000000', '0', '1161212400000'),
('41', '0', 'NPC', 'NPC Clan', 'ClanHall', '41', '0', 'The Bronze Chamber', '1', '50000000', '0', '1161212400000'),
('42', '0', 'NPC', 'NPC Clan', 'ClanHall', '42', '0', 'The Golden Chamber', '1', '50000000', '0', '1161212400000'),
('43', '0', 'NPC', 'NPC Clan', 'ClanHall', '43', '0', 'The Silver Chamber', '1', '50000000', '0', '1161212400000'),
('44', '0', 'NPC', 'NPC Clan', 'ClanHall', '44', '0', 'The Mithril Chamber', '1', '50000000', '0', '1161212400000'),
('45', '0', 'NPC', 'NPC Clan', 'ClanHall', '45', '0', 'The Bronze Chamber', '1', '50000000', '0', '1161212400000'),
('46', '0', 'NPC', 'NPC Clan', 'ClanHall', '46', '0', 'Silver Manor', '1', '50000000', '0', '1161212400000'),
('47', '0', 'NPC', 'NPC Clan', 'ClanHall', '47', '0', 'Moonstone Hall', '1', '50000000', '0', '1161212400000'),
('48', '0', 'NPC', 'NPC Clan', 'ClanHall', '48', '0', 'Onyx Hall', '1', '50000000', '0', '1161212400000'),
('49', '0', 'NPC', 'NPC Clan', 'ClanHall', '49', '0', 'Emerald Hall', '1', '50000000', '0', '1161212400000'),
('50', '0', 'NPC', 'NPC Clan', 'ClanHall', '50', '0', 'Sapphire Hall', '1', '50000000', '0', '1161212400000'),
('51', '0', 'NPC', 'NPC Clan', 'ClanHall', '51', '0', 'Mont Chamber', '1', '50000000', '0', '1161212400000'),
('52', '0', 'NPC', 'NPC Clan', 'ClanHall', '52', '0', 'Astaire Chamber', '1', '50000000', '0', '1161212400000'),
('53', '0', 'NPC', 'NPC Clan', 'ClanHall', '53', '0', 'Aria Chamber', '1', '50000000', '0', '1161212400000'),
('54', '0', 'NPC', 'NPC Clan', 'ClanHall', '54', '0', 'Yiana Chamber', '1', '50000000', '0', '1161212400000'),
('55', '0', 'NPC', 'NPC Clan', 'ClanHall', '55', '0', 'Roien Chamber', '1', '50000000', '0', '1161212400000'),
('56', '0', 'NPC', 'NPC Clan', 'ClanHall', '56', '0', 'Luna Chamber', '1', '50000000', '0', '1161212400000'),
('57', '0', 'NPC', 'NPC Clan', 'ClanHall', '57', '0', 'Traban Chamber', '1', '50000000', '0', '1161212400000'),
('58', '0', 'NPC', 'NPC Clan', 'ClanHall', '58', '0', 'Eisen Hall', '1', '50000000', '0', '1161212400000'),
('59', '0', 'NPC', 'NPC Clan', 'ClanHall', '59', '0', 'Heavy Metal Hall', '1', '50000000', '0', '1161212400000'),
('60', '0', 'NPC', 'NPC Clan', 'ClanHall', '60', '0', 'Molten Ore Hall', '1', '50000000', '0', '1161212400000'),
('61', '0', 'NPC', 'NPC Clan', 'ClanHall', '61', '0', 'Titan Hall', '1', '50000000', '0', '1161212400000');


-- 
-- Table structure for table `auction_bid`
-- 

DROP TABLE IF EXISTS `auction_bid`;
CREATE TABLE `auction_bid` (
  `id` int(11) NOT NULL default '0',
  `auctionId` int(11) NOT NULL default '0',
  `bidderId` int(11) NOT NULL default '0',
  `bidderName` varchar(50) NOT NULL default '',
  `maxBid` int(11) NOT NULL default '0',
  `clan_name` varchar(50) NOT NULL default '',
  `time_bid` decimal(20,0) NOT NULL default '0',
  PRIMARY KEY  (`auctionId`,`bidderId`),
  KEY `id` (`id`)
);


-- 
-- Table structure for table `clanhall`
-- 

DROP TABLE IF EXISTS `clanhall`;
CREATE TABLE `clanhall` (
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
