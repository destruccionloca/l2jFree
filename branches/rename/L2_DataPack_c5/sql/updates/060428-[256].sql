insert into zone values(1,'EVMotherTree','EVMotherTree',47600,38290,44483,41745,-3491,0);

alter table `fish` add column `speed` int NOT NULL default 0;

ALTER TABLE `random_spawn_loc` ADD COLUMN `heading` INTEGER NOT NULL DEFAULT -1,
DROP PRIMARY KEY,
ADD PRIMARY KEY(`groupId`, `x`, `y`, `z`, `heading`);
