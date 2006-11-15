ALTER TABLE character_recipebook ADD type INT NOT NULL DEFAULT 0;
UPDATE character_recipebook set type = 1;
ALTER TABLE `clan_data` ADD `crest_large_id` INT( 11 ) AFTER `crest_id`;
UPDATE `etcitem` SET `consume_type`='stackable' WHERE `oldtype`='recipe';