ALTER TABLE `characters` ADD COLUMN in_jail decimal(1,0) DEFAULT 0;
ALTER TABLE `characters` ADD COLUMN jail_timer decimal(20,0) DEFAULT 0;

insert into zone values (1, 'Monster Derby Track', 'Monster Derby Track', 11600, 181200, 14600, 184500, -3565, 0);