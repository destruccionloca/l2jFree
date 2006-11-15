ALTER TABLE `spawnlist` ADD `periodOfDay` decimal(2,0) default 0;
update spawnlist set periodOfDay = 0;