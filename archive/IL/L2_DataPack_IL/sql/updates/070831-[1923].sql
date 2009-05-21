UPDATE `characters` SET `lvl_joined_academy` = `academy_lvl` WHERE `lvl_joined_academy` = 0 AND `academy_lvl` != 0;
ALTER TABLE `characters` DROP `academy_lvl`;