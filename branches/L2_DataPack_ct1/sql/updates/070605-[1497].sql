ALTER TABLE `tvt` ADD `joinTime` INT( 11 ) NOT NULL DEFAULT '0' AFTER `teamsCount` ,
ADD `eventTime` INT( 11 ) NOT NULL DEFAULT '0' AFTER `joinTime` ;
