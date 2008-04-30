ALTER TABLE `clan_subpledges` ADD COLUMN `leader_id` INTEGER NOT NULL DEFAULT 0;
UPDATE `clan_subpledges` , `characters` SET clan_subpledges.leader_id = characters.obj_id WHERE clan_subpledges.leader_name = characters.char_name;
ALTER TABLE `clan_subpledges` DROP COLUMN `leader_name`;

ALTER TABLE `armor`
CHANGE `item_skill_id` `item_skill_id` VARCHAR(60) NOT NULL DEFAULT '0',
CHANGE `item_skill_lvl` `item_skill_lvl` VARCHAR(30) NOT NULL DEFAULT '0';

ALTER TABLE `weapon`
CHANGE `item_skill_id` `item_skill_id` VARCHAR(60) NOT NULL DEFAULT '0',
CHANGE `item_skill_lvl` `item_skill_lvl` VARCHAR(30) NOT NULL DEFAULT '0';