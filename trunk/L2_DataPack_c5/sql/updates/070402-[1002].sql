ALTER TABLE `auto_chat` 
ADD `groupName` VARCHAR( 128 ) NOT NULL AFTER `groupId`,
ADD `chatRange` SMALLINT NOT NULL DEFAULT '-1', 
ADD `chatRandom` BOOL NOT NULL DEFAULT '0';