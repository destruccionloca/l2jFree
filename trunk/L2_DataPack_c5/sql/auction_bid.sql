-- 
-- Table structure for table `auction_bid`
-- 

CREATE TABLE IF NOT EXISTS `auction_bid` (
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
