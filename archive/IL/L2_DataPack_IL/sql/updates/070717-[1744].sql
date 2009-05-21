ALTER TABLE `tvt` ADD `npcHeading` INT( 11 ) NOT NULL DEFAULT '0' AFTER `npcZ` ;
ALTER TABLE `tvt` ADD `minPlayers` INT( 4 ) NOT NULL DEFAULT '0',
ADD `maxPlayers` INT( 4 ) NOT NULL DEFAULT '0';
